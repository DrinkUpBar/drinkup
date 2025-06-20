package cool.drinkup.drinkup.user.internal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import cool.drinkup.drinkup.user.internal.model.OAuthTypeEnum;
import cool.drinkup.drinkup.user.internal.model.User;
import cool.drinkup.drinkup.user.internal.model.UserOAuth;

/**
 * 用户OAuth绑定信息Repository
 */
@Repository
public interface UserOAuthRepository extends JpaRepository<UserOAuth, Long> {

    /**
     * 根据OAuth ID和类型查找绑定
     */
    Optional<UserOAuth> findByOauthIdAndOauthType(String oauthId, OAuthTypeEnum oauthType);

    /**
     * 根据OAuth ID和类型查找启用的绑定
     */
    Optional<UserOAuth> findByOauthIdAndOauthTypeAndEnabledTrue(String oauthId, OAuthTypeEnum oauthType);

    /**
     * 根据用户查找所有OAuth绑定
     */
    List<UserOAuth> findByUser(User user);

    /**
     * 根据用户查找所有启用的OAuth绑定
     */
    List<UserOAuth> findByUserAndEnabledTrue(User user);

    /**
     * 根据用户和OAuth类型查找绑定
     */
    Optional<UserOAuth> findByUserAndOauthType(User user, OAuthTypeEnum oauthType);

    /**
     * 根据用户和OAuth类型查找启用的绑定
     */
    Optional<UserOAuth> findByUserAndOauthTypeAndEnabledTrue(User user, OAuthTypeEnum oauthType);

    /**
     * 根据用户查找主要的OAuth绑定
     */
    Optional<UserOAuth> findByUserAndIsPrimaryTrue(User user);

    /**
     * 检查用户是否已经绑定了指定类型的OAuth
     */
    boolean existsByUserAndOauthTypeAndEnabledTrue(User user, OAuthTypeEnum oauthType);

    /**
     * 检查OAuth ID和类型的组合是否已存在（用于避免重复绑定）
     */
    boolean existsByOauthIdAndOauthType(String oauthId, OAuthTypeEnum oauthType);

    /**
     * 将用户的所有OAuth绑定设置为非主要
     */
    @Modifying
    @Query("UPDATE UserOAuth uo SET uo.isPrimary = false WHERE uo.user = :user")
    void unsetAllPrimaryForUser(@Param("user") User user);

    /**
     * 根据用户和OAuth类型删除绑定
     */
    void deleteByUserAndOauthType(User user, OAuthTypeEnum oauthType);

    /**
     * 根据用户删除所有OAuth绑定
     */
    void deleteByUser(User user);

    /**
     * 统计用户的OAuth绑定数量
     */
    int countByUserAndEnabledTrue(User user);

    /**
     * 根据OAuth邮箱查找绑定（用于邮箱匹配登录）
     */
    List<UserOAuth> findByOauthEmailAndEnabledTrue(String oauthEmail);
}