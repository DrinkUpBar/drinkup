package cool.drinkup.drinkup.infrastructure.internal.image.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;

import java.util.Map;

import ai.fal.client.ClientConfig;
import ai.fal.client.CredentialsResolver;
import ai.fal.client.FalClient;
import ai.fal.client.queue.QueueResultOptions;
import ai.fal.client.queue.QueueStatus;
import ai.fal.client.queue.QueueStatusOptions;
import ai.fal.client.queue.QueueSubmitOptions;
import cool.drinkup.drinkup.infrastructure.internal.image.config.properties.FalProperties;
import cool.drinkup.drinkup.infrastructure.spi.ImageGenerator;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FalImageGenerator implements ImageGenerator {

    private final FalProperties falProperties;

    private final FalClient falClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public FalImageGenerator(FalProperties falProperties) {
        this.falProperties = falProperties;
        this.falClient = FalClient.withConfig(ClientConfig.withCredentials(CredentialsResolver.fromApiKey(falProperties.getApiKey())));
    }

    @Override
    public String generateImage(String prompt) {
        var input = falProperties.getImageProperties();
        input.setPrompt(prompt);
        var inputMap = objectMapper.convertValue(input, Map.class);
        var job =  falClient.queue().submit(this.falProperties.getEndpointId(), QueueSubmitOptions.withInput(inputMap));
        var requestId = job.getRequestId();
        var result = falClient.queue().status(this.falProperties.getEndpointId(), QueueStatusOptions.withRequestId(requestId));
        long startTime = System.currentTimeMillis();
        long timeoutMillis = this.falProperties.getTimeout();

        while (result.getStatus() != QueueStatus.Status.COMPLETED) {
            if (System.currentTimeMillis() - startTime > timeoutMillis) {
                throw new RuntimeException("Image generation timed out after 30 seconds");
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error("Error waiting for job to complete", e);
            }
            log.info("Waiting for job to complete, current status: {}, elapsed time: {}s", 
                result.getStatus(),
                (System.currentTimeMillis() - startTime) / 1000);
            result = falClient.queue().status(this.falProperties.getEndpointId(), QueueStatusOptions.withRequestId(requestId));
        }
        var output = falClient.queue().result(this.falProperties.getEndpointId(), QueueResultOptions.withRequestId(requestId));
        JsonObject data = output.getData();
        log.info("Fal image generation result: {}", data.toString());
        var images = data.getAsJsonArray("images");
        var firstImage = images.get(0).getAsJsonObject();
        return firstImage.get("url").getAsString();
    }

}
