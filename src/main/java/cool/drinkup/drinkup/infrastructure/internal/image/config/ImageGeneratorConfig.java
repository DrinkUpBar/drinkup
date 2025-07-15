package cool.drinkup.drinkup.infrastructure.internal.image.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import cool.drinkup.drinkup.infrastructure.internal.image.config.properties.FalProperties;
import cool.drinkup.drinkup.infrastructure.internal.image.config.properties.GlifProperties;
import cool.drinkup.drinkup.infrastructure.internal.image.impl.fal.FalImageGeneratorFactory;
import cool.drinkup.drinkup.infrastructure.internal.image.impl.glif.GlifImageGeneratorFactory;
import cool.drinkup.drinkup.infrastructure.spi.image.ImageGenerator;
import cool.drinkup.drinkup.infrastructure.spi.image.config.FalConfig;
import cool.drinkup.drinkup.infrastructure.spi.image.config.GlifConfig;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class ImageGeneratorConfig {

    @Resource
    private GlifImageGeneratorFactory glifImageGeneratorFactory;

    @Resource
    private FalImageGeneratorFactory falImageGeneratorFactory;

    @Bean
    @Primary
    public ImageGenerator glifImageGenerator(GlifProperties properties, ObjectMapper objectMapper) {
        GlifConfig config = GlifConfig.builder()
                .apiUrl(properties.getApiUrl())
                .bearerToken(properties.getBearerToken())
                .glifId(properties.getGlifId())
                .weights(properties.getWeights())
                .build();
        return glifImageGeneratorFactory.create(config);
    }

    @Bean
    public ImageGenerator falImageGenerator(FalProperties falProperties) {
        FalConfig config = FalConfig.builder()
                .apiKey(falProperties.getApiKey())
                .endpointId(falProperties.getEndpointId())
                .timeout(falProperties.getTimeout())
                .imageProperties(falProperties.getImageProperties())
                .build();
        return falImageGeneratorFactory.create(config);
    }
}
