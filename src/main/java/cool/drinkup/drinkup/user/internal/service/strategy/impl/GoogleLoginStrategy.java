package cool.drinkup.drinkup.user.internal.service.strategy.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import cool.drinkup.drinkup.user.internal.controller.req.LoginRequest;
import cool.drinkup.drinkup.user.internal.model.OAuthTypeEnum;
import cool.drinkup.drinkup.user.internal.model.RoleEnum;
import cool.drinkup.drinkup.user.internal.model.User;
import cool.drinkup.drinkup.user.internal.service.GoogleTokenService;
import cool.drinkup.drinkup.user.internal.service.UserOAuthService;
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
 * 改进版Google登录策略 - 支持多种OAuth方式登录
 * 相比原版，此策略支持：
 * 1. 一个用户绑定多种OAuth账号
 * 2. 智能的用户匹配和绑定
 * 3. OAuth信息的完整管理
 */
@Slf4j
@Component("googleLoginStrategy")
@RequiredArgsConstructor
public class GoogleLoginStrategy implements LoginStrategy {

    private final GoogleTokenService googleTokenService;
    private final UserService userService;
    private final UserOAuthService userOAuthService;

    @Override
    public LoginRequest.LoginType getLoginType() {
        return LoginRequest.LoginType.GOOGLE;
    }

    @Override
    public boolean validateCredentials(LoginRequest loginRequest) {
        String idToken = loginRequest.getIdToken();

        if (!StringUtils.hasText(idToken)) {
            log.warn("Google ID 令牌为空");
            return false;
        }

        try {
            GoogleIdToken token = googleTokenService.verifyIdToken(idToken);
            return token != null;
        } catch (Exception e) {
            log.error("Google 令牌验证失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public LoginResult getOrCreateUser(LoginRequest loginRequest) {
        try {
            GoogleIdToken token = googleTokenService.verifyIdToken(loginRequest.getIdToken());
            if (token == null) {
                throw new RuntimeException("无效的 Google 令牌");
            }

            GoogleIdToken.Payload payload = token.getPayload();
            String oauthId = payload.getSubject();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String picture = (String) payload.get("picture");

            // 1. 首先尝试通过OAuth ID直接查找已绑定的用户
            Optional<User> existingUser = userOAuthService.findUserByOAuth(oauthId, OAuthTypeEnum.GOOGLE);
            if (existingUser.isPresent()) {
                User user = existingUser.get();
                // 更新最后使用时间
                userOAuthService.updateLastUsedDate(oauthId, OAuthTypeEnum.GOOGLE);
                log.info("用户 {} 通过已绑定的Google账号登录", user.getId());
                return new LoginResult(user, false);
            }

            // 2. 如果通过OAuth ID找不到，尝试通过邮箱查找现有用户
            Optional<User> userByEmail = findUserByEmail(email);
            if (userByEmail.isPresent()) {
                User user = userByEmail.get();

                // 检查用户是否已经绑定了Google账号
                if (userOAuthService.hasOAuthBinding(user, OAuthTypeEnum.GOOGLE)) {
                    log.warn("用户 {} 已绑定其他Google账号，无法绑定新的Google账号", user.getId());
                    throw new RuntimeException("该邮箱对应的用户已绑定其他Google账号");
                }

                // 为现有用户绑定新的Google账号
                userOAuthService.createOAuthBinding(user, oauthId, OAuthTypeEnum.GOOGLE, email, name, picture);

                log.info("为现有用户 {} 绑定了新的Google账号", user.getId());
                return new LoginResult(user, false);
            }

            // 3. 如果都找不到，创建新用户
            User newUser = createNewUserWithGoogleBinding(oauthId, email, name, picture);
            log.info("创建新用户 {} 并绑定Google账号", newUser.getId());
            return new LoginResult(newUser, true);

        } catch (Exception e) {
            log.error("获取或创建 Google 用户失败: {}", e.getMessage());
            throw new RuntimeException("Google 登录失败: " + e.getMessage());
        }
    }

    @Override
    public String getUserIdentifier(LoginRequest loginRequest) {
        try {
            GoogleIdToken token = googleTokenService.verifyIdToken(loginRequest.getIdToken());
            if (token != null) {
                return token.getPayload().getEmail();
            }
        } catch (Exception e) {
            log.error("获取 Google 用户标识符失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 通过邮箱查找用户（优先使用OAuth邮箱，其次使用主邮箱）
     */
    private Optional<User> findUserByEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return Optional.empty();
        }

        // 先通过OAuth邮箱查找
        Optional<User> userByOAuthEmail = userOAuthService.findUserByOAuthEmail(email);
        if (userByOAuthEmail.isPresent()) {
            return userByOAuthEmail;
        }

        // 再通过主邮箱查找
        return userService.findByEmail(email);
    }

    /**
     * 创建新用户并绑定Google账号
     */
    private User createNewUserWithGoogleBinding(String oauthId, String email, String name, String picture) {
        log.info("创建新Google用户，邮箱: {}", email);

        // 创建用户基本信息
        User user = new User();
        user.setUsername(generateUniqueUsername(name, email));
        user.setNickname(StringUtils.hasText(name) ? name : "Google用户");
        user.setEmail(email);
        user.setAvatar(picture);
        user.setPassword(userService.generateRandomPassword());
        user.setEnabled(true);

        // 设置默认角色
        Set<String> roles = new HashSet<>();
        roles.add(RoleEnum.USER.getRole());
        user.setRoles(roles);

        // 保存用户
        User savedUser = userService.save(user);

        // 创建OAuth绑定
        userOAuthService.createOAuthBinding(savedUser, oauthId, OAuthTypeEnum.GOOGLE, email, name, picture);

        return savedUser;
    }

    /**
     * 生成唯一的用户名
     */
    private String generateUniqueUsername(String name, String email) {
        String baseUsername;

        if (StringUtils.hasText(name)) {
            baseUsername = name.replaceAll("[^a-zA-Z0-9]", "");
        } else if (StringUtils.hasText(email)) {
            baseUsername = email.split("@")[0].replaceAll("[^a-zA-Z0-9]", "");
        } else {
            baseUsername = "google_user";
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
