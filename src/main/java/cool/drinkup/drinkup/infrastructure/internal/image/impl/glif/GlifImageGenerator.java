package cool.drinkup.drinkup.infrastructure.internal.image.impl.glif;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cool.drinkup.drinkup.infrastructure.internal.image.impl.dto.GlifImageRequest;
import cool.drinkup.drinkup.infrastructure.spi.image.ImageGenerator;
import cool.drinkup.drinkup.infrastructure.spi.image.config.GlifConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
public class GlifImageGenerator implements ImageGenerator {

    private final RestClient restClient = RestClient.builder().build();
    private final ObjectMapper objectMapper;
    private final GlifConfig config;

    private GlifImageGenerator(GlifConfig config, ObjectMapper objectMapper) {
        this.config = config;
        this.objectMapper = objectMapper;
    }

    public static GlifImageGenerator create(GlifConfig config, ObjectMapper objectMapper) {
        return new GlifImageGenerator(config, objectMapper);
    }

    @Override
    public String generateImage(String prompt) {
        try {
            String finalPrompt = prompt + ", " + config.triggerWord();
            GlifImageRequest.Input input = new GlifImageRequest.Input(config.weights(), finalPrompt);
            GlifImageRequest glifImageRequest = new GlifImageRequest(config.glifId(), input);
            String response = restClient
                    .post()
                    .uri(config.apiUrl())
                    .header("Authorization", config.bearerToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(glifImageRequest)
                    .retrieve()
                    .body(String.class);
            log.info("Glif API response: {}", response);
            JsonNode jsonNode = objectMapper.readTree(response);
            if (jsonNode.has("error") && !jsonNode.get("error").isNull()) {
                throw new RuntimeException(
                        "Glif API error: " + jsonNode.get("error").asText());
            }
            return jsonNode.get("output").asText();
        } catch (RestClientException e) {
            log.error("Failed to generate image with Glif API, error: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate image: " + e.getMessage());
        } catch (Exception e) {
            log.error("Failed to parse Glif API response, error: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to parse API response: " + e.getMessage());
        }
    }
}
