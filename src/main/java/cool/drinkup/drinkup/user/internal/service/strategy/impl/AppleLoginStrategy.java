package cool.drinkup.drinkup.user.internal.service.strategy.impl;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import cool.drinkup.drinkup.user.internal.controller.req.LoginRequest;
import cool.drinkup.drinkup.user.internal.model.OAuthTypeEnum;
import cool.drinkup.drinkup.user.internal.model.RoleEnum;
import cool.drinkup.drinkup.user.internal.model.User;
import cool.drinkup.drinkup.user.internal.service.AppleTokenService;
import cool.drinkup.drinkup.user.internal.service.UserOAuthService;
import cool.drinkup.drinkup.user.internal.service.UserService;
import cool.drinkup.drinkup.user.internal.service.strategy.LoginStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Apple 登录策略 - 支持多种OAuth方式登录
 * 
 * 改进功能：
 * 1. 支持一个用户绑定多种OAuth账号
 * 2. 智能的用户匹配和绑定逻辑
 * 3. 完整的OAuth信息管理
 * 4. 处理Apple特有的私密邮箱场景
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AppleLoginStrategy implements LoginStrategy {

    private final AppleTokenService appleTokenService;
    private final UserService userService;
    private final UserOAuthService userOAuthService;

    @Override
    public LoginRequest.LoginType getLoginType() {
        return LoginRequest.LoginType.APPLE;
    }

    @Override
    public boolean validateCredentials(LoginRequest loginRequest) {
        String idToken = loginRequest.getIdToken();

        if ( !StringUtils.hasText(idToken)) {
            log.warn("Apple ID 令牌为空");
            return false;
        }

        try {
            Map<String, Object> userInfo = appleTokenService.verifyIdToken(idToken);
            return userInfo != null;
        } catch (Exception e) {
            log.error("Apple 令牌验证失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public LoginResult getOrCreateUser(LoginRequest loginRequest) {
        try {
            Map<String, Object> userInfo = appleTokenService.verifyIdToken(loginRequest.getIdToken());
            if ( userInfo == null) {
                throw new RuntimeException("无效的 Apple 令牌");
            }

            String oauthId = (String) userInfo.get("sub");
            String email = (String) userInfo.get("email");
            Boolean emailVerified = (Boolean) userInfo.get("email_verified");
            Boolean isPrivateEmail = (Boolean) userInfo.get("is_private_email");

            // 1. 首先尝试通过OAuth ID直接查找已绑定的用户
            Optional<User> existingUser = userOAuthService.findUserByOAuth(oauthId, OAuthTypeEnum.APPLE);
            if ( existingUser.isPresent()) {
                User user = existingUser.get();
                // 更新最后使用时间
                userOAuthService.updateLastUsedDate(oauthId, OAuthTypeEnum.APPLE);
                log.info("用户 {} 通过已绑定的Apple账号登录", user.getId());
                return new LoginResult(user, false);
            }

            // 2. 如果通过OAuth ID找不到，且邮箱不为空且非私密邮箱，尝试通过邮箱查找现有用户
            if ( StringUtils.hasText(email) && (isPrivateEmail == null || !isPrivateEmail)) {
                Optional<User> userByEmail = findUserByEmail(email);
                if ( userByEmail.isPresent()) {
                    User user = userByEmail.get();

                    // 检查用户是否已经绑定了Apple账号
                    if ( userOAuthService.hasOAuthBinding(user, OAuthTypeEnum.APPLE)) {
                        log.warn("用户 {} 已绑定其他Apple账号，无法绑定新的Apple账号", user.getId());
                        throw new RuntimeException("该邮箱对应的用户已绑定其他Apple账号");
                    }

                    // 为现有用户绑定新的Apple账号
                    userOAuthService.createOAuthBinding(user, oauthId, OAuthTypeEnum.APPLE,
                            email, extractUsernameFromEmail(email), null);

                    log.info("为现有用户 {} 绑定了新的Apple账号", user.getId());
                    return new LoginResult(user, false);
                }
            }

            // 3. 如果都找不到，创建新用户
            User newUser = createNewUserWithAppleBinding(oauthId, email, emailVerified, isPrivateEmail);
            log.info("创建新用户 {} 并绑定Apple账号", newUser.getId());
            return new LoginResult(newUser, true);

        } catch (Exception e) {
            log.error("获取或创建 Apple 用户失败: {}", e.getMessage());
            throw new RuntimeException("Apple 登录失败: " + e.getMessage());
        }
    }

    @Override
    public String getUserIdentifier(LoginRequest loginRequest) {
        try {
            Map<String, Object> userInfo = appleTokenService.verifyIdToken(loginRequest.getIdToken());
            if ( userInfo != null) {
                return (String) userInfo.get("email");
            }
        } catch (Exception e) {
            log.error("获取 Apple 用户标识符失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 通过邮箱查找用户（优先使用OAuth邮箱，其次使用主邮箱）
     */
    private Optional<User> findUserByEmail(String email) {
        if ( !StringUtils.hasText(email)) {
            return Optional.empty();
        }

        // 先通过OAuth邮箱查找
        Optional<User> userByOAuthEmail = userOAuthService.findUserByOAuthEmail(email);
        if ( userByOAuthEmail.isPresent()) {
            return userByOAuthEmail;
        }

        // 再通过主邮箱查找
        return userService.findByEmail(email);
    }

    /**
     * 从邮箱中提取用户名
     */
    private String extractUsernameFromEmail(String email) {
        if ( !StringUtils.hasText(email)) {
            return null;
        }
        return email.split("@")[0];
    }

    /**
     * 创建新用户并绑定Apple账号
     */
    private User createNewUserWithAppleBinding(String oauthId, String email, Boolean emailVerified,
            Boolean isPrivateEmail) {
        log.info("创建新Apple用户，邮箱: {}, 私密邮箱: {}", email, isPrivateEmail);

        // 创建用户基本信息
        User user = new User();

        // 处理用户名和昵称
        String displayName;
        if ( isPrivateEmail != null && isPrivateEmail) {
            // 私密邮箱，生成临时显示名
            displayName = "Apple用户" + System.currentTimeMillis() % 10000;
        } else if ( StringUtils.hasText(email)) {
            // 有效邮箱，使用邮箱前缀
            displayName = extractUsernameFromEmail(email);
        } else {
            // 没有邮箱，使用默认名称
            displayName = "Apple用户";
        }

        user.setUsername(generateUniqueUsername(displayName));
        user.setNickname(displayName);
        user.setEmail(StringUtils.hasText(email) ? email : "");
        user.setPassword(userService.generateRandomPassword());
        user.setEnabled(true);

        // 设置默认角色
        Set<String> roles = new HashSet<>();
        roles.add(RoleEnum.USER.getRole());
        user.setRoles(roles);

        // 保存用户
        User savedUser = userService.save(user);

        // 如果没有提供邮箱或是私密邮箱，使用生成的ID更新昵称
        if ( !StringUtils.hasText(email) || (isPrivateEmail != null && isPrivateEmail)) {
            savedUser.setNickname("品鉴师" + String.format("%06d", savedUser.getId()));
            savedUser = userService.save(savedUser);
        }

        // 创建OAuth绑定
        String oauthUsername = StringUtils.hasText(email) ? extractUsernameFromEmail(email) : null;
        userOAuthService.createOAuthBinding(savedUser, oauthId, OAuthTypeEnum.APPLE,
                email, oauthUsername, null);

        return savedUser;
    }

    /**
     * 生成唯一的用户名
     */
    private String generateUniqueUsername(String baseUsername) {
        if ( !StringUtils.hasText(baseUsername)) {
            baseUsername = "apple_user";
        }

        // 清理用户名，只保留字母数字
        baseUsername = baseUsername.replaceAll("[^a-zA-Z0-9]", "");
        if ( baseUsername.isEmpty()) {
            baseUsername = "apple_user";
        }

        // 确保用户名唯一
        String username = baseUsername;
        int counter = 1;
        while (userService.findByUsername(username).isPresent()) {
            username = baseUsername + "_" + counter++;
        }

        return username;
    }
}