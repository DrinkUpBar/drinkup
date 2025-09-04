package cool.drinkup.drinkup.infrastructure.internal.sms.impl;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import cool.drinkup.drinkup.infrastructure.internal.sms.config.AliyunSmsProperties;
import cool.drinkup.drinkup.infrastructure.spi.sms.SmsSender;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;

@Slf4j
@RequiredArgsConstructor
public class AliyunSmsSender implements SmsSender {

    private final AliyunSmsProperties properties;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, String> redisTemplate;
    private Client client;

    private static final String SMS_IDEMPOTENCY_KEY_PREFIX = "sms:idempotency:";
    private static final int MAX_PHONE_NUMBERS = 1000;

    private synchronized Client getClient() throws Exception {
        if (client == null) {
            Config config = new Config()
                    .setAccessKeyId(properties.getAccessKeyId())
                    .setAccessKeySecret(properties.getAccessKeySecret())
                    .setEndpoint("dysmsapi.aliyuncs.com");
            client = new Client(config);
        }
        return client;
    }

    @Override
    public void sendSms(String phoneNumber, String message) {
        if (phoneNumber.equals("13800138000")) {
            return;
        }
        // 验证手机号格式
        validatePhoneNumber(phoneNumber);
        if (properties.isSkipVerification()) {
            log.info("Skipping verification for phone number: {}", phoneNumber);
            return;
        }
        // 幂等性检查
        String idempotencyKey = SMS_IDEMPOTENCY_KEY_PREFIX + phoneNumber + ":" + message;
        if (isDuplicateMessage(idempotencyKey)) {
            log.warn("Duplicate SMS message detected for phone number: {}", phoneNumber);
            return;
        }

        int retryCount = 0;
        Exception lastException = null;

        while (retryCount < properties.getMaxRetries()) {
            try {
                // 构建请求参数
                SendSmsRequest sendSmsRequest = new SendSmsRequest()
                        .setPhoneNumbers(phoneNumber)
                        .setSignName(properties.getSignName())
                        .setTemplateCode(properties.getTemplateCode());

                // 支持灵活的模板参数
                Map<String, String> templateParams;
                if (message.startsWith("{") && message.endsWith("}")) {
                    // 如果message已经是JSON格式，直接使用
                    templateParams = objectMapper.readValue(message, new TypeReference<Map<String, String>>() {});
                } else {
                    // 否则，使用默认的code参数
                    templateParams = Map.of("code", message);
                }
                sendSmsRequest.setTemplateParam(objectMapper.writeValueAsString(templateParams));

                // 发送请求
                SendSmsResponse response = getClient().sendSms(sendSmsRequest);
                String code = response.getBody().getCode();
                String bizId = response.getBody().getBizId();

                log.info("SMS response: {}", response.getBody());

                if ("OK".equals(code)) {
                    // 发送成功，记录幂等性信息到Redis，设置过期时间
                    redisTemplate
                            .opsForValue()
                            .set(
                                    idempotencyKey,
                                    String.valueOf(System.currentTimeMillis()),
                                    properties.getVerificationCodeExpireMinutes(),
                                    TimeUnit.MINUTES);
                    log.info("SMS sent successfully to {}, BizId: {}", phoneNumber, bizId);
                    return;
                } else {
                    // 处理特定错误码
                    handleErrorCode(
                            code,
                            Map.of("Code", code, "Message", response.getBody().getMessage()));
                    lastException = new RuntimeException(
                            "Failed to send SMS: " + response.getBody().getMessage());
                }
            } catch (Exception e) {
                lastException = e;
                log.warn("Attempt {} failed to send SMS to {}: {}", retryCount + 1, phoneNumber, e.getMessage());
            }

            retryCount++;
            if (retryCount < properties.getMaxRetries()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        log.error("Failed to send SMS to {} after {} attempts", phoneNumber, properties.getMaxRetries(), lastException);
        throw new RuntimeException(
                "Failed to send SMS after " + properties.getMaxRetries() + " attempts", lastException);
    }

    private void validatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be empty");
        }

        // 检查是否是批量号码
        String[] phoneNumbers = phoneNumber.split(",");
        if (phoneNumbers.length > MAX_PHONE_NUMBERS) {
            throw new IllegalArgumentException("Number of phone numbers exceeds maximum limit of " + MAX_PHONE_NUMBERS);
        }

        for (String number : phoneNumbers) {
            number = number.trim();
            // 国内手机号格式：+86/+/0086/86/空 + 1开头11位数字
            // 国际/港澳台号码格式：+ + 国际区号 + 号码
            if (!number.matches("^(\\+86|\\+|0086|86)?1[3-9]\\d{9}$")
                    && // 国内号码
                    !number.matches("^\\+[1-9]\\d{1,14}$")) { // 国际号码
                throw new IllegalArgumentException("Invalid phone number format: " + number);
            }
        }
    }

    private boolean isDuplicateMessage(String idempotencyKey) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(idempotencyKey));
    }

    private void handleErrorCode(String code, Map<String, Object> responseMap) {
        String message = (String) responseMap.get("Message");
        switch (code) {
            case "isv.BUSINESS_LIMIT_CONTROL":
                log.error("Business limit control: {}", message);
                throw new RuntimeException("SMS sending frequency limit exceeded: " + message);
            case "isv.INVALID_PARAMETERS":
                log.error("Invalid parameters: {}", message);
                throw new IllegalArgumentException("Invalid SMS parameters: " + message);
            case "isv.SMS_SIGNATURE_ILLEGAL":
                log.error("SMS signature illegal: {}", message);
                throw new RuntimeException("Invalid SMS signature: " + message);
            case "isv.SMS_TEMPLATE_ILLEGAL":
                log.error("SMS template illegal: {}", message);
                throw new RuntimeException("Invalid SMS template: " + message);
            case "isv.MOBILE_NUMBER_ILLEGAL":
                log.error("Mobile number illegal: {}", message);
                throw new IllegalArgumentException("Invalid mobile number: " + message);
            case "isv.AMOUNT_NOT_ENOUGH":
                log.error("Insufficient balance: {}", message);
                throw new RuntimeException("Insufficient SMS balance: " + message);
            default:
                log.error("Unknown error code: {}, message: {}", code, message);
                throw new RuntimeException("SMS sending failed: " + message);
        }
    }

    @Override
    public boolean verifySms(String phoneNumber, String code) {
        // 验证手机号格式
        validatePhoneNumber(phoneNumber);

        if (properties.isSkipVerification()) {
            log.info("Skipping verification for phone number: {}", phoneNumber);
            return true;
        }

        // 如果是批量号码，只验证第一个号码
        String firstPhoneNumber = phoneNumber.split(",")[0].trim();

        // 从Redis中获取验证码
        String key = SMS_IDEMPOTENCY_KEY_PREFIX + firstPhoneNumber + ":" + code;
        String storedCode = redisTemplate.opsForValue().get(key);

        if (storedCode == null) {
            log.warn("No verification code found for phone number: {}", firstPhoneNumber);
            return false;
        }

        // 验证码
        boolean isValid = StringUtils.hasText(storedCode);

        if (isValid) {
            // 验证成功后删除验证码，防止重复使用
            redisTemplate.delete(key);
            log.info("SMS verification successful for phone number: {}", firstPhoneNumber);
        } else {
            log.warn("Invalid verification code for phone number: {}", firstPhoneNumber);
        }

        return isValid;
    }

    /**
     * 批量验证短信验证码
     * @param phoneNumbers 手机号列表
     * @param codes 验证码列表
     * @return 验证结果列表
     */
    public List<Boolean> verifySmsBatch(List<String> phoneNumbers, List<String> codes) {
        if (phoneNumbers.size() != codes.size()) {
            throw new IllegalArgumentException("Phone numbers and codes lists must have the same size");
        }

        return phoneNumbers.stream()
                .map(phoneNumber -> {
                    int index = phoneNumbers.indexOf(phoneNumber);
                    return verifySms(phoneNumber, codes.get(index));
                })
                .toList();
    }
}
