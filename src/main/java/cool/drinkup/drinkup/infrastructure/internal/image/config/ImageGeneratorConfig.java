package cool.drinkup.drinkup.infrastructure.internal.image.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import cool.drinkup.drinkup.infrastructure.internal.image.config.properties.FalProperties;
import cool.drinkup.drinkup.infrastructure.internal.image.config.properties.GlifProperties;
import cool.drinkup.drinkup.infrastructure.internal.image.impl.FalImageGenerator;
import cool.drinkup.drinkup.infrastructure.internal.image.impl.GlifImageGenerator;
import cool.drinkup.drinkup.infrastructure.spi.ImageGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class ImageGeneratorConfig {

    @Bean
    @Primary
    public ImageGenerator glifImageGenerator(GlifProperties properties, ObjectMapper objectMapper) {
        return new GlifImageGenerator(properties, objectMapper);
    }

    @Bean
    public ImageGenerator falImageGenerator(FalProperties falProperties) {
        return new FalImageGenerator(falProperties);
    }
}
