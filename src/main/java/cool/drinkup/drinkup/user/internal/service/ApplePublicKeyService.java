package cool.drinkup.drinkup.user.internal.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cool.drinkup.drinkup.user.internal.model.ApplePublicKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Apple 公钥服务
 * 负责获取、缓存和管理Apple公钥
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApplePublicKeyService {

    private static final String APPLE_PUBLIC_KEYS_URL = "https://appleid.apple.com/auth/keys";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    // 内存缓存，存储公钥
    private final Map<String, PublicKey> publicKeyCache = new ConcurrentHashMap<>();

    /**
     * 根据密钥ID获取公钥
     * 
     * @param keyId 密钥ID
     * @return 公钥，如果未找到则返回null
     */
    public PublicKey getPublicKey(String keyId) {
        // 先从缓存中获取
        PublicKey cachedKey = publicKeyCache.get(keyId);
        if (cachedKey != null) {
            log.debug("从缓存中获取到公钥，keyId: {}", keyId);
            return cachedKey;
        }

        // 缓存中没有，从Apple服务器获取
        return fetchAndCachePublicKey(keyId);
    }

    /**
     * 从Apple服务器获取公钥并缓存
     * 
     * @param keyId 密钥ID
     * @return 公钥，如果未找到则返回null
     */
    @Cacheable(value = "applePublicKeys", key = "#keyId", unless = "#result == null")
    private PublicKey fetchAndCachePublicKey(String keyId) {
        try {
            log.info("从Apple服务器获取公钥，keyId: {}", keyId);

            // 获取Apple公钥列表
            ApplePublicKey applePublicKey = getApplePublicKeys();
            if (applePublicKey == null || applePublicKey.getKeys() == null) {
                log.error("无法获取Apple公钥列表");
                return null;
            }

            // 查找指定的密钥
            ApplePublicKey.Key targetKey = applePublicKey.getKeys().stream()
                    .filter(key -> keyId.equals(key.getKid()))
                    .findFirst()
                    .orElse(null);

            if (targetKey == null) {
                log.error("未找到指定的公钥，keyId: {}", keyId);
                return null;
            }

            // 构建RSA公钥
            PublicKey publicKey = buildRSAPublicKey(targetKey.getN(), targetKey.getE());
            if (publicKey != null) {
                // 缓存公钥
                publicKeyCache.put(keyId, publicKey);
                log.info("成功获取并缓存Apple公钥，keyId: {}", keyId);
            }

            return publicKey;

        } catch (Exception e) {
            log.error("获取Apple公钥失败，keyId: {}, error: {}", keyId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取Apple公钥列表
     * 
     * @return Apple公钥列表
     */
    private ApplePublicKey getApplePublicKeys() {
        try {
            String response = restClient.get()
                    .uri(APPLE_PUBLIC_KEYS_URL)
                    .retrieve()
                    .body(String.class);
            return objectMapper.readValue(response, ApplePublicKey.class);
        } catch (Exception e) {
            log.error("获取Apple公钥列表失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 根据模数和指数构建RSA公钥
     * 
     * @param modulus  模数 (n)
     * @param exponent 指数 (e)
     * @return RSA公钥
     */
    private PublicKey buildRSAPublicKey(String modulus, String exponent) {
        try {
            // Base64URL解码
            byte[] nBytes = Base64.getUrlDecoder().decode(modulus);
            byte[] eBytes = Base64.getUrlDecoder().decode(exponent);

            // 转换为BigInteger
            BigInteger n = new BigInteger(1, nBytes);
            BigInteger e = new BigInteger(1, eBytes);

            // 构建RSA公钥规范
            RSAPublicKeySpec spec = new RSAPublicKeySpec(n, e);

            // 生成公钥
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(spec);

        } catch (Exception e) {
            log.error("构建RSA公钥失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 清除公钥缓存
     */
    public void clearCache() {
        publicKeyCache.clear();
        log.info("已清除Apple公钥缓存");
    }
}