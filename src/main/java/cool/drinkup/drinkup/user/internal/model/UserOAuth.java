package cool.drinkup.drinkup.user.internal.model;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 用户OAuth绑定信息实体
 * 支持一个用户绑定多种OAuth方式登录
 */
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_oauth", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "oauth_id", "oauth_type" })
})
@Getter
@Setter
public class UserOAuth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联的用户
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * OAuth提供商的唯一标识
     */
    @Column(name = "oauth_id", nullable = false)
    private String oauthId;

    /**
     * OAuth提供商类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "oauth_type", nullable = false)
    private OAuthTypeEnum oauthType;

    /**
     * OAuth相关的邮箱（可能与用户主邮箱不同）
     */
    @Column(name = "oauth_email")
    private String oauthEmail;

    /**
     * OAuth提供商返回的用户名
     */
    @Column(name = "oauth_username")
    private String oauthUsername;

    /**
     * OAuth提供商返回的头像URL
     */
    @Column(name = "oauth_avatar")
    private String oauthAvatar;

    /**
     * 是否是主要的OAuth绑定（第一个绑定的或用户指定的主要方式）
     */
    @Column(name = "is_primary")
    private boolean isPrimary = false;

    /**
     * 是否启用此OAuth绑定
     */
    @Column(name = "enabled")
    private boolean enabled = true;

    /**
     * OAuth绑定的创建时间
     */
    @CreationTimestamp
    @Column(name = "create_date", updatable = false, columnDefinition = "DATETIME")
    private ZonedDateTime createDate = ZonedDateTime.now(ZoneOffset.UTC);

    /**
     * OAuth绑定的更新时间
     */
    @UpdateTimestamp
    @Column(name = "update_date", columnDefinition = "DATETIME")
    private ZonedDateTime updateDate = ZonedDateTime.now(ZoneOffset.UTC);

    /**
     * 最后一次使用此OAuth方式登录的时间
     */
    @Column(name = "last_used_date", columnDefinition = "DATETIME")
    private ZonedDateTime lastUsedDate;

    /**
     * 构造方法：创建新的OAuth绑定
     */
    public UserOAuth(User user, String oauthId, OAuthTypeEnum oauthType, String oauthEmail) {
        this.user = user;
        this.oauthId = oauthId;
        this.oauthType = oauthType;
        this.oauthEmail = oauthEmail;
        this.enabled = true;
        this.lastUsedDate = ZonedDateTime.now(ZoneOffset.UTC);
    }

    /**
     * 更新最后使用时间
     */
    public void updateLastUsedDate() {
        this.lastUsedDate = ZonedDateTime.now(ZoneOffset.UTC);
    }
}