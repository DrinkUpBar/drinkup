package cool.drinkup.drinkup.infrastructure.internal.image.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cool.drinkup.drinkup.infrastructure.internal.image.config.properties.ImageProcessorProperties;
import cool.drinkup.drinkup.infrastructure.spi.ImageProcessor;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@RequiredArgsConstructor
public class RustImageProcessor implements ImageProcessor {
    private final RestClient restClient = RestClient.builder().build();
    private final ImageProcessorProperties properties;
    private final ObjectMapper objectMapper;

    @Override
    public String removeBackground(String imageBase64) {
        try {
            // 构建请求体
            Map<String, String> requestBody = Map.of("imageData", imageBase64, "outputFormat", "png");

            String response = restClient
                    .post()
                    .uri(properties.getApiHost() + "/process")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            log.info("Image processor API response received");

            // 解析响应
            JsonNode jsonNode = objectMapper.readTree(response);

            // 检查响应码
            if (jsonNode.has("code") && jsonNode.get("code").asInt() != 0) {
                String message =
                        jsonNode.has("message") ? jsonNode.get("message").asText() : "Unknown error";
                throw new RuntimeException("Image processor API error: " + message);
            }

            // 从 data.processedImage 获取处理后的图像数据
            if (jsonNode.has("data") && jsonNode.get("data").has("processedImage")) {
                return jsonNode.get("data").get("processedImage").asText();
            } else {
                throw new RuntimeException("Invalid API response format: missing data.processedImage field");
            }

        } catch (RestClientException e) {
            log.error("Failed to process image with background removal API, error: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to remove background: " + e.getMessage());
        } catch (Exception e) {
            log.error("Failed to parse image processor API response, error: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to parse API response: " + e.getMessage());
        }
    }
}
