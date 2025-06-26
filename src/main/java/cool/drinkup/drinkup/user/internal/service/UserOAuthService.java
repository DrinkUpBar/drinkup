package cool.drinkup.drinkup.user.internal.service;

import cool.drinkup.drinkup.user.internal.model.OAuthTypeEnum;
import cool.drinkup.drinkup.user.internal.model.User;
import cool.drinkup.drinkup.user.internal.model.UserOAuth;
import cool.drinkup.drinkup.user.internal.repository.UserOAuthRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户OAuth绑定服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserOAuthService {

    private final UserOAuthRepository userOAuthRepository;

    /**
     * 根据OAuth ID和类型查找用户
     */
    @Transactional(readOnly = true)
    public Optional<User> findUserByOAuth(String oauthId, OAuthTypeEnum oauthType) {
        return userOAuthRepository
                .findByOauthIdAndOauthTypeAndEnabledTrue(oauthId, oauthType)
                .map(UserOAuth::getUser);
    }

    /**
     * 根据OAuth ID和类型查找OAuth绑定
     */
    @Transactional(readOnly = true)
    public Optional<UserOAuth> findByOauthIdAndType(String oauthId, OAuthTypeEnum oauthType) {
        return userOAuthRepository.findByOauthIdAndOauthTypeAndEnabledTrue(oauthId, oauthType);
    }

    /**
     * 获取用户的所有OAuth绑定
     */
    @Transactional(readOnly = true)
    public List<UserOAuth> getUserOAuthBindings(User user) {
        return userOAuthRepository.findByUserAndEnabledTrue(user);
    }

    /**
     * 检查用户是否已绑定指定类型的OAuth
     */
    @Transactional(readOnly = true)
    public boolean hasOAuthBinding(User user, OAuthTypeEnum oauthType) {
        return userOAuthRepository.existsByUserAndOauthTypeAndEnabledTrue(user, oauthType);
    }

    /**
     * 为用户创建新的OAuth绑定
     */
    @Transactional
    public UserOAuth createOAuthBinding(
            User user,
            String oauthId,
            OAuthTypeEnum oauthType,
            String oauthEmail,
            String oauthUsername,
            String oauthAvatar) {

        // 检查是否已存在相同的OAuth绑定
        if (userOAuthRepository.existsByOauthIdAndOauthType(oauthId, oauthType)) {
            throw new IllegalArgumentException("OAuth绑定已存在: " + oauthType + " - " + oauthId);
        }

        // 检查用户是否已绑定同类型的OAuth
        if (hasOAuthBinding(user, oauthType)) {
            throw new IllegalArgumentException("用户已绑定" + oauthType + "账号");
        }

        UserOAuth userOAuth = new UserOAuth(user, oauthId, oauthType, oauthEmail);
        userOAuth.setOauthUsername(oauthUsername);
        userOAuth.setOauthAvatar(oauthAvatar);

        // 如果这是用户的第一个OAuth绑定，设置为主要绑定
        int existingBindingsCount = userOAuthRepository.countByUserAndEnabledTrue(user);
        if (existingBindingsCount == 0) {
            userOAuth.setPrimary(true);
        }

        UserOAuth saved = userOAuthRepository.save(userOAuth);
        log.info("为用户 {} 创建了新的 {} OAuth绑定", user.getId(), oauthType);

        return saved;
    }

    /**
     * 更新OAuth绑定信息
     */
    @Transactional
    public UserOAuth updateOAuthBinding(
            UserOAuth userOAuth, String oauthEmail, String oauthUsername, String oauthAvatar) {
        userOAuth.setOauthEmail(oauthEmail);
        userOAuth.setOauthUsername(oauthUsername);
        userOAuth.setOauthAvatar(oauthAvatar);
        userOAuth.updateLastUsedDate();

        return userOAuthRepository.save(userOAuth);
    }

    /**
     * 设置主要OAuth绑定
     */
    @Transactional
    public void setPrimaryOAuth(User user, OAuthTypeEnum oauthType) {
        // 首先将用户的所有OAuth绑定设置为非主要
        userOAuthRepository.unsetAllPrimaryForUser(user);

        // 然后设置指定类型为主要
        Optional<UserOAuth> oauthBinding = userOAuthRepository.findByUserAndOauthTypeAndEnabledTrue(user, oauthType);
        if (oauthBinding.isPresent()) {
            UserOAuth userOAuth = oauthBinding.get();
            userOAuth.setPrimary(true);
            userOAuthRepository.save(userOAuth);
            log.info("为用户 {} 设置 {} 为主要OAuth绑定", user.getId(), oauthType);
        } else {
            throw new IllegalArgumentException("用户未绑定该类型的OAuth账号: " + oauthType);
        }
    }

    /**
     * 解绑OAuth账号
     */
    @Transactional
    public void unbindOAuth(User user, OAuthTypeEnum oauthType) {
        Optional<UserOAuth> oauthBinding = userOAuthRepository.findByUserAndOauthTypeAndEnabledTrue(user, oauthType);
        if (oauthBinding.isPresent()) {
            UserOAuth userOAuth = oauthBinding.get();

            // 检查是否是最后一个OAuth绑定（保留至少一个登录方式）
            int bindingCount = userOAuthRepository.countByUserAndEnabledTrue(user);
            if (bindingCount <= 1 && (user.getPhone() == null || user.getPhone().isEmpty())) {
                throw new IllegalArgumentException("不能解绑最后一个登录方式，请先绑定手机号或其他OAuth账号");
            }

            // 如果解绑的是主要绑定，需要设置新的主要绑定
            if (userOAuth.isPrimary()) {
                userOAuth.setPrimary(false);
                userOAuthRepository.save(userOAuth);

                // 选择另一个OAuth绑定作为主要绑定
                List<UserOAuth> otherBindings = userOAuthRepository.findByUserAndEnabledTrue(user);
                if (!otherBindings.isEmpty()) {
                    UserOAuth newPrimary = otherBindings.get(0);
                    newPrimary.setPrimary(true);
                    userOAuthRepository.save(newPrimary);
                }
            }

            // 禁用OAuth绑定而不是删除（保留历史记录）
            userOAuth.setEnabled(false);
            userOAuthRepository.save(userOAuth);

            log.info("用户 {} 解绑了 {} OAuth账号", user.getId(), oauthType);
        } else {
            throw new IllegalArgumentException("用户未绑定该类型的OAuth账号: " + oauthType);
        }
    }

    /**
     * 更新OAuth绑定的最后使用时间
     */
    @Transactional
    public void updateLastUsedDate(String oauthId, OAuthTypeEnum oauthType) {
        Optional<UserOAuth> oauthBinding =
                userOAuthRepository.findByOauthIdAndOauthTypeAndEnabledTrue(oauthId, oauthType);
        if (oauthBinding.isPresent()) {
            UserOAuth userOAuth = oauthBinding.get();
            userOAuth.updateLastUsedDate();
            userOAuthRepository.save(userOAuth);
        }
    }

    /**
     * 根据邮箱查找可能的用户（用于邮箱匹配登录）
     */
    @Transactional(readOnly = true)
    public Optional<User> findUserByOAuthEmail(String email) {
        List<UserOAuth> oauthBindings = userOAuthRepository.findByOauthEmailAndEnabledTrue(email);
        return oauthBindings.isEmpty()
                ? Optional.empty()
                : Optional.of(oauthBindings.get(0).getUser());
    }
}
