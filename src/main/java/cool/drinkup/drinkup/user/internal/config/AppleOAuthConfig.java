package cool.drinkup.drinkup.user.internal.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * Apple OAuth 配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "apple.oauth")
public class AppleOAuthConfig {

    /**
     * Apple OAuth 客户端 ID (Bundle ID)
     */
    private String clientId;

    /**
     * Apple Team ID
     */
    private String teamId;

    /**
     * Apple Key ID
     */
    private String keyId;

    /**
     * Apple 私钥文件路径
     */
    private String privateKeyPath;

    /**
     * Apple JWT 令牌过期时间（秒）
     */
    private long tokenExpiration = 3600; // 1小时

    /**
     * Apple 验证 URL
     */
    private String verifyUrl = "https://appleid.apple.com/auth/keys";
}