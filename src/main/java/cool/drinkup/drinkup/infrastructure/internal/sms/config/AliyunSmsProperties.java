package cool.drinkup.drinkup.infrastructure.internal.sms.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import cool.drinkup.drinkup.infrastructure.internal.sms.impl.AliyunSmsSender;
import cool.drinkup.drinkup.infrastructure.spi.SmsSender;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

@Data
@Configuration
@ConfigurationProperties(prefix = "aliyun.sms")
public class AliyunSmsProperties {
    private String accessKeyId;
    private String accessKeySecret;
    private String signName;
    private String templateCode;
    private String endpoint = "https://dysmsapi.aliyuncs.com";
    private int maxRetries = 3;
    private long verificationCodeExpireMinutes = 5;
    private boolean skipVerification = false;

    @Bean
    SmsSender smsSender(
            AliyunSmsProperties properties, ObjectMapper objectMapper, RedisTemplate<String, String> redisTemplate) {
        return new AliyunSmsSender(properties, objectMapper, redisTemplate);
    }
}
