package cool.drinkup.drinkup.infrastructure.internal.image.impl;

import cool.drinkup.drinkup.infrastructure.internal.image.config.properties.ImgProxyProperties;
import cool.drinkup.drinkup.infrastructure.spi.ImageCompressor;
import io.micrometer.observation.annotation.Observed;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ImgProxyImageCompressor implements ImageCompressor {

    private final ImgProxyProperties properties;

    @Observed(
            name = "image.imgproxy.compress",
            contextualName = "Imgproxy压缩图片",
            lowCardinalityKeyValues = {"Tag", "image"})
    @Override
    public String compress(String imageUrl) {
        return buildSignedUrl(imageUrl, properties.getParam());
    }

    private String buildSignedUrl(String imageUrl, String processingParam) {
        try {
            String encodedSourceUrl = URLEncoder.encode(imageUrl, StandardCharsets.UTF_8);
            String path = "/" + processingParam + "/plain/" + encodedSourceUrl;

            byte[] key = hexToBytes(properties.getKey());
            byte[] salt = hexToBytes(properties.getSalt());

            // HMAC-SHA256 签名
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(key, "HmacSHA256");
            hmac.init(keySpec);
            hmac.update(salt);
            byte[] signature = hmac.doFinal(path.getBytes(StandardCharsets.UTF_8));

            // base64url 编码签名
            String encodedSignature = Base64.getUrlEncoder().withoutPadding().encodeToString(signature);

            // 拼接最终 URL，确保没有重复的斜杠
            String baseUrl = properties.getUrl();
            if (baseUrl.endsWith("/")) {
                return baseUrl + encodedSignature + path;
            } else {
                return baseUrl + "/" + encodedSignature + path;
            }

        } catch (Exception e) {
            throw new RuntimeException("生成 imgproxy URL 失败", e);
        }
    }

    private static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] bytes = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            bytes[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4) + Character.digit(hex.charAt(i + 1), 16));
        }
        return bytes;
    }
}
