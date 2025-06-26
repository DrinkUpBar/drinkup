package cool.drinkup.drinkup.user.internal.service;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.security.PublicKey;
import java.util.Date;
import java.util.List;
import java.util.Map;

import cool.drinkup.drinkup.user.internal.config.AppleOAuthConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.IncorrectClaimException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Apple Token 验证服务
 * 实现完整的生产级JWT验证逻辑，包括签名验证、公钥管理等
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppleTokenService {

    private final AppleOAuthConfig appleOAuthConfig;
    private final ApplePublicKeyService applePublicKeyService;
    private final ObjectMapper objectMapper;

    /**
     * 验证 Apple ID Token
     * 实现完整的JWT验证，包括签名验证、时间验证等
     *
     * @param idToken Apple ID Token
     * @return 包含用户信息的Map，如果验证失败则返回null
     */
    @Retryable(value = { Exception.class }, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public Map<String, Object> verifyIdToken(String idToken) {
        try {
            log.info("开始验证Apple ID Token");

            // 1. 解析JWT头部获取密钥ID
            String keyId = extractKeyIdFromToken(idToken);
            if (keyId == null) {
                log.error("无法从Token中提取密钥ID");
                return null;
            }

            // 2. 获取对应的公钥
            PublicKey publicKey = applePublicKeyService.getPublicKey(keyId);
            if (publicKey == null) {
                log.error("无法获取公钥，keyId: {}", keyId);
                return null;
            }

            // 3. 验证JWT签名和声明
            Claims claims = parseAndValidateJwt(idToken, publicKey);
            if (claims == null) {
                log.error("JWT验证失败");
                return null;
            }

            // 4. 提取用户信息
            Map<String, Object> userInfo = extractUserInfo(claims);

            log.info("Apple ID Token验证成功，用户ID: {}", userInfo.get("sub"));
            return userInfo;

        } catch (Exception e) {
            log.error("Apple ID Token验证失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 从JWT Token中提取密钥ID
     *
     * @param idToken JWT Token
     * @return 密钥ID
     */
    private String extractKeyIdFromToken(String idToken) {
        try {
            // 检查 idToken 是否为 null 或空
            if (idToken == null || idToken.trim().isEmpty()) {
                log.error("Apple ID Token为空或null");
                return null;
            }

            // 解析JWT头部
            String[] tokenParts = idToken.split("\\.");
            if (tokenParts.length != 3) {
                log.error("Apple ID Token格式无效");
                return null;
            }

            // 解码头部
            String header = new String(java.util.Base64.getUrlDecoder().decode(tokenParts[0]));
            JsonNode headerNode = objectMapper.readTree(header);

            return headerNode.get("kid").asText();

        } catch (Exception e) {
            log.error("提取密钥ID失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 解析和验证JWT
     *
     * @param idToken   JWT Token
     * @param publicKey 公钥
     * @return JWT声明，验证失败返回null
     */
    private Claims parseAndValidateJwt(String idToken, PublicKey publicKey) {
        try {
            // 获取所有有效的客户端ID列表
            List<String> validClientIds = appleOAuthConfig.getAllClientIds();
            if (validClientIds.isEmpty()) {
                log.error("未配置有效的Apple客户端ID");
                return null;
            }

            log.debug("开始验证JWT，支持的客户端ID数量: {}", validClientIds.size());

            // 尝试使用每个客户端ID进行验证
            Claims validClaims = null;
            String matchedClientId = null;

            for (String clientId : validClientIds) {
                try {
                    // 使用当前客户端ID构建解析器
                    JwtParser parser = Jwts.parser()
                            .verifyWith(publicKey)
                            .requireIssuer("https://appleid.apple.com")
                            .requireAudience(clientId)
                            .build();

                    Jws<Claims> jws = parser.parseSignedClaims(idToken);
                    validClaims = jws.getPayload();
                    matchedClientId = clientId;

                    log.info("JWT验证成功，匹配的客户端ID: {}", clientId);
                    break; // 找到匹配的客户端ID，退出循环

                } catch (io.jsonwebtoken.security.SecurityException
                        | MalformedJwtException
                        | UnsupportedJwtException
                        | IllegalArgumentException
                        | IncorrectClaimException e) {
                    // 这些异常表示当前客户端ID不匹配，继续尝试下一个
                    log.debug("客户端ID {} 验证失败: {}", clientId, e.getMessage());
                    continue;
                } catch (ExpiredJwtException e) {
                    // Token已过期，无论哪个客户端ID都会失败
                    log.error("JWT已过期: {}", e.getMessage());
                    return null;
                }
            }

            // 如果所有客户端ID都验证失败
            if (validClaims == null) {
                log.error("JWT验证失败，Token的受众(audience)不匹配任何配置的客户端ID");
                return null;
            }

            // 额外验证过期时间（JJWT会自动验证，这里是双重保险）
            Date expiration = validClaims.getExpiration();
            if (expiration != null && expiration.before(new Date())) {
                log.error("Token已过期: {}", expiration);
                return null;
            }

            log.info("JWT签名验证成功，使用的客户端ID: {}", matchedClientId);
            return validClaims;

        } catch (ExpiredJwtException e) {
            log.error("JWT已过期: {}", e.getMessage());
            return null;
        } catch (UnsupportedJwtException e) {
            log.error("不支持的JWT格式: {}", e.getMessage());
            return null;
        } catch (MalformedJwtException e) {
            log.error("JWT格式错误: {}", e.getMessage());
            return null;
        } catch (SignatureException e) {
            log.error("JWT签名验证失败: {}", e.getMessage());
            return null;
        } catch (IllegalArgumentException e) {
            log.error("JWT参数错误: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("JWT验证异常: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 从JWT声明中提取用户信息
     *
     * @param claims JWT声明
     * @return 用户信息Map
     */
    private Map<String, Object> extractUserInfo(Claims claims) {
        try {
            String subject = claims.getSubject();
            String email = claims.get("email", String.class);
            Boolean emailVerified = claims.get("email_verified", Boolean.class);
            Boolean isPrivateEmail = claims.get("is_private_email", Boolean.class);
            Long authTime = claims.get("auth_time", Long.class);

            // 构建用户信息Map
            Map<String, Object> userInfo = Map.of(
                    "sub", subject != null ? subject : "",
                    "email", email != null ? email : "",
                    "email_verified", emailVerified != null ? emailVerified : false,
                    "is_private_email", isPrivateEmail != null ? isPrivateEmail : false,
                    "auth_time", authTime != null ? authTime : 0L,
                    "iss", claims.getIssuer(),
                    "aud", claims.getAudience(),
                    "exp", claims.getExpiration().getTime() / 1000,
                    "iat", claims.getIssuedAt().getTime() / 1000);

            log.debug("提取的用户信息: {}", userInfo);
            return userInfo;

        } catch (Exception e) {
            log.error("提取用户信息失败: {}", e.getMessage(), e);
            return Map.of();
        }
    }

    /**
     * 验证Token的基本格式
     *
     * @param idToken JWT Token
     * @return 是否格式正确
     */
    public boolean isValidTokenFormat(String idToken) {
        if (idToken == null || idToken.trim().isEmpty()) {
            return false;
        }

        String[] parts = idToken.split("\\.");
        return parts.length == 3;
    }

    /**
     * 清除公钥缓存（用于重试机制）
     */
    public void clearPublicKeyCache() {
        applePublicKeyService.clearCache();
        log.info("已清除Apple公钥缓存");
    }
}
