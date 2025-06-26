package cool.drinkup.drinkup.user.internal.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Google OAuth 配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "google.oauth")
public class GoogleOAuthConfig {

    /**
     * Google OAuth 客户端 ID
     */
    private String clientId;

    private String authorizeUrl = "https://proxy.fjhdream.cn/oauth2/authorize/";

    private String tokenUrl = "https://proxy.fjhdream.cn/oauth2/token/";

    private String certsUrl = "https://proxy.fjhdream.cn/oauth2/certs/";
}
