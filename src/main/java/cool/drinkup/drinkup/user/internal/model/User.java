package cool.drinkup.drinkup.user.internal.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = true, unique = true)
    private String email;

    /**
     * @deprecated 使用 oauthBindings 代替，保留此字段用于向后兼容
     *             将在后续版本中移除
     */
    @Deprecated
    @Column(nullable = true)
    private String oauthId;

    /**
     * @deprecated 使用 oauthBindings 代替，保留此字段用于向后兼容
     *             将在后续版本中移除
     */
    @Deprecated
    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private OAuthTypeEnum oauthType;

    /**
     * 用户的OAuth绑定列表，支持多种OAuth方式登录
     */
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<UserOAuth> oauthBindings;

    @Column(nullable = true)
    private String avatar;

    @Column(nullable = true)
    private String password;

    @Column(nullable = true, unique = true)
    private String phone;

    private boolean enabled = true;

    @CreationTimestamp
    @Column(name = "create_date", updatable = false, columnDefinition = "DATETIME")
    private ZonedDateTime createDate = ZonedDateTime.now(ZoneOffset.UTC);

    @UpdateTimestamp
    @Column(name = "update_date", columnDefinition = "DATETIME")
    private ZonedDateTime updateDate = ZonedDateTime.now(ZoneOffset.UTC);

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<String> roles;

    /**
     * 获取主要的OAuth绑定
     */
    public UserOAuth getPrimaryOAuthBinding() {
        if (oauthBindings == null) {
            return null;
        }
        return oauthBindings.stream()
                .filter(UserOAuth::isPrimary)
                .filter(UserOAuth::isEnabled)
                .findFirst()
                .orElse(null);
    }

    /**
     * 检查是否绑定了指定类型的OAuth
     */
    public boolean hasOAuthBinding(OAuthTypeEnum oauthType) {
        if (oauthBindings == null) {
            return false;
        }
        return oauthBindings.stream().anyMatch(binding -> binding.getOauthType() == oauthType && binding.isEnabled());
    }

    /**
     * 获取指定类型的OAuth绑定
     */
    public UserOAuth getOAuthBinding(OAuthTypeEnum oauthType) {
        if (oauthBindings == null) {
            return null;
        }
        return oauthBindings.stream()
                .filter(binding -> binding.getOauthType() == oauthType && binding.isEnabled())
                .findFirst()
                .orElse(null);
    }
}
