package cool.drinkup.drinkup.user.internal.service;

import com.google.api.client.auth.openidconnect.IdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import cool.drinkup.drinkup.user.internal.config.GoogleOAuthConfig;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Google 令牌验证服务
 */
@Slf4j
@Service
public class GoogleTokenService {

    private final IdTokenVerifier verifier;

    public GoogleTokenService(
            @Value("${google.oauth.client-id}") String primaryClientId,
            @Value("${google.oauth.client-ids:#{null}}") List<String> clientIds,
            GoogleOAuthConfig googleOAuthConfig) {

        // 如果配置了多个客户端ID，使用列表；否则使用单个客户端ID
        List<String> audienceList =
                clientIds != null && !clientIds.isEmpty() ? clientIds : Arrays.asList(primaryClientId);

        NetHttpTransport transport = new NetHttpTransport();
        this.verifier = new GoogleIdTokenVerifier.Builder(transport, new GsonFactory())
                .setAudience(audienceList)
                .setIssuer("https://accounts.google.com")
                .setCertificatesLocation(googleOAuthConfig.getCertsUrl())
                .build();

        log.info("Google Token验证器已初始化，支持的客户端ID数量: {}", audienceList.size());
    }

    /**
     * 验证 Google ID 令牌
     * @param idTokenString ID 令牌字符串
     * @return 验证成功返回 GoogleIdToken，否则返回 null
     */
    public GoogleIdToken verifyIdToken(String idTokenString) {
        try {
            GoogleIdToken idToken = GoogleIdToken.parse(new GsonFactory(), idTokenString);
            Boolean verified = verifier.verify(idToken);
            if (idToken != null && verified) {
                GoogleIdToken.Payload payload = idToken.getPayload();

                // 验证令牌是否已经过期
                if (payload.getExpirationTimeSeconds() < System.currentTimeMillis() / 1000) {
                    log.warn("Google ID 令牌已过期");
                    return null;
                }

                // 记录客户端ID信息（用于调试）
                Object audienceObj = payload.getAudience();
                String audience = audienceObj != null ? audienceObj.toString() : "unknown";
                log.info("Google ID 令牌验证成功，用户: {}, 客户端ID: {}", payload.getEmail(), audience);
                return idToken;
            } else {
                log.warn("Google ID 令牌验证失败：令牌无效或客户端ID不匹配");
                return null;
            }
        } catch (Exception e) {
            log.error("Google ID 令牌验证异常: {}", e.getMessage());
            return null;
        }
    }
}
