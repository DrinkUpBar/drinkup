package cool.drinkup.drinkup.infrastructure.internal.image.impl.fal;

import ai.fal.client.ClientConfig;
import ai.fal.client.CredentialsResolver;
import ai.fal.client.FalClient;
import ai.fal.client.queue.QueueResultOptions;
import ai.fal.client.queue.QueueStatus;
import ai.fal.client.queue.QueueStatusOptions;
import ai.fal.client.queue.QueueSubmitOptions;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import cool.drinkup.drinkup.infrastructure.spi.image.ImageGenerator;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FalImageGenerator implements ImageGenerator {

    private final FalConfig config;
    private final FalClient falClient;
    private final ObjectMapper objectMapper;

    private FalImageGenerator(FalConfig config, ObjectMapper objectMapper) {
        this.config = config;
        this.objectMapper = objectMapper;
        this.falClient =
                FalClient.withConfig(ClientConfig.withCredentials(CredentialsResolver.fromApiKey(config.apiKey())));
    }

    public static FalImageGenerator create(FalConfig config, ObjectMapper objectMapper) {
        return new FalImageGenerator(config, objectMapper);
    }

    @Override
    public String generateImage(String prompt) {
        try {
            var input = config.imageProperties();
            input.setPrompt(prompt);
            var inputMap = objectMapper.convertValue(input, Map.class);
            var job = falClient.queue().submit(config.endpointId(), QueueSubmitOptions.withInput(inputMap));
            var requestId = job.getRequestId();
            var result = falClient.queue().status(config.endpointId(), QueueStatusOptions.withRequestId(requestId));
            long startTime = System.currentTimeMillis();
            long timeoutMillis = config.timeout();

            while (result.getStatus() != QueueStatus.Status.COMPLETED) {
                if (System.currentTimeMillis() - startTime > timeoutMillis) {
                    throw new RuntimeException(
                            "Image generation timed out after " + (timeoutMillis / 1000) + " seconds");
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    log.error("Error waiting for job to complete", e);
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Image generation was interrupted", e);
                }
                log.info(
                        "Waiting for job to complete, current status: {}, elapsed time: {}s",
                        result.getStatus(),
                        (System.currentTimeMillis() - startTime) / 1000);
                result = falClient.queue().status(config.endpointId(), QueueStatusOptions.withRequestId(requestId));
            }
            var output = falClient.queue().result(config.endpointId(), QueueResultOptions.withRequestId(requestId));
            JsonObject data = output.getData();
            log.info("Fal image generation result: {}", data.toString());
            var images = data.getAsJsonArray("images");
            if (images == null || images.size() == 0) {
                throw new RuntimeException("No images returned from Fal API");
            }
            var firstImage = images.get(0).getAsJsonObject();
            return firstImage.get("url").getAsString();
        } catch (Exception e) {
            log.error("Failed to generate image with Fal API, error: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate image: " + e.getMessage(), e);
        }
    }
}
