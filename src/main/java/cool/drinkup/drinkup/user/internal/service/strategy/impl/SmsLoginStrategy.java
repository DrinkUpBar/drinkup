package cool.drinkup.drinkup.user.internal.service.strategy.impl;

import cool.drinkup.drinkup.infrastructure.spi.SmsSender;
import cool.drinkup.drinkup.user.internal.controller.req.LoginRequest;
import cool.drinkup.drinkup.user.internal.mapper.UserMapper;
import cool.drinkup.drinkup.user.internal.model.RoleEnum;
import cool.drinkup.drinkup.user.internal.model.User;
import cool.drinkup.drinkup.user.internal.service.UserService;
import cool.drinkup.drinkup.user.internal.service.strategy.LoginStrategy;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 手机号验证码登录策略
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SmsLoginStrategy implements LoginStrategy {

    private final SmsSender smsSender;
    private final UserService userService;
    private final UserMapper userMapper;

    @Override
    public LoginRequest.LoginType getLoginType() {
        return LoginRequest.LoginType.SMS;
    }

    @Override
    public boolean validateCredentials(LoginRequest loginRequest) {
        String phoneNumber = loginRequest.getPhone();
        String verificationCode = loginRequest.getVerificationCode();

        if (!StringUtils.hasText(phoneNumber) || !StringUtils.hasText(verificationCode)) {
            log.warn("手机号或验证码为空");
            return false;
        }

        // 测试用户直接通过
        if (isTestUser(phoneNumber, verificationCode)) {
            log.info("测试用户登录: {}", phoneNumber);
            return true;
        }

        // 验证验证码
        try {
            return smsSender.verifySms(phoneNumber, verificationCode);
        } catch (Exception e) {
            log.error("验证码验证失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public LoginResult getOrCreateUser(LoginRequest loginRequest) {
        String phoneNumber = loginRequest.getPhone();

        // 查找现有用户
        Optional<User> user = userService.findByPhone(phoneNumber);
        if (user.isEmpty()) {
            User newUser = createNewUser(loginRequest);
            return new LoginResult(newUser, true);
        }
        return new LoginResult(user.get(), false);
    }

    @Override
    public String getUserIdentifier(LoginRequest loginRequest) {
        return loginRequest.getPhone();
    }

    private User createNewUser(LoginRequest loginRequest) {
        log.info("创建新用户，手机号: {}", loginRequest.getPhone());

        User user = userMapper.toUser(loginRequest);
        user.setUsername(generateRandomUsername());
        user.setNickname("品鉴师000000");
        user.setPassword(userService.generateRandomPassword());

        // 设置默认角色
        Set<String> roles = new HashSet<>();
        roles.add(RoleEnum.USER.getRole());
        user.setRoles(roles);

        // 保存用户并获取ID
        user = userService.save(user);

        // 使用ID更新昵称并再次保存
        user.setNickname("品鉴师" + String.format("%06d", user.getId()));
        return userService.save(user);
    }

    private String generateRandomUsername() {
        StringBuilder randomUsername = new StringBuilder();
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < 16; i++) {
            randomUsername.append(alphabet.charAt(random.nextInt(alphabet.length())));
        }
        return randomUsername.toString();
    }

    private boolean isTestUser(String phoneNumber, String verificationCode) {
        return "13800138000".equalsIgnoreCase(phoneNumber) && "250528".equalsIgnoreCase(verificationCode);
    }
}
