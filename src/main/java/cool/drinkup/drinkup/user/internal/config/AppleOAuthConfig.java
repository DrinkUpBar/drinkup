package cool.drinkup.drinkup.user.internal.config;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Apple OAuth 配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "apple.oauth")
public class AppleOAuthConfig {

    /**
     * Apple OAuth 客户端 ID (Bundle ID) - 主要客户端ID，保持向后兼容
     */
    private String clientId;

    /**
     * Apple OAuth 多个客户端 ID 列表 - 支持多应用场景
     * 如果配置了此项，将优先使用此配置进行验证
     */
    private List<String> clientIds = new ArrayList<>();

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

    /**
     * 获取所有有效的客户端ID列表
     * 优先返回clientIds列表，如果为空则返回包含单个clientId的列表
     *
     * @return 有效的客户端ID列表
     */
    public List<String> getAllClientIds() {
        if (clientIds != null && !clientIds.isEmpty()) {
            return new ArrayList<>(clientIds);
        }

        if (clientId != null && !clientId.trim().isEmpty()) {
            List<String> singleClientIdList = new ArrayList<>();
            singleClientIdList.add(clientId);
            return singleClientIdList;
        }

        return new ArrayList<>();
    }

    /**
     * 检查给定的客户端ID是否在允许的列表中
     *
     * @param targetClientId 要检查的客户端ID
     * @return 如果客户端ID有效则返回true
     */
    public boolean isValidClientId(String targetClientId) {
        if (targetClientId == null || targetClientId.trim().isEmpty()) {
            return false;
        }

        return getAllClientIds().contains(targetClientId);
    }

    /**
     * 获取主要的客户端ID（用于向后兼容）
     *
     * @return 主要的客户端ID
     */
    public String getPrimaryClientId() {
        List<String> allIds = getAllClientIds();
        return allIds.isEmpty() ? null : allIds.get(0);
    }
}
