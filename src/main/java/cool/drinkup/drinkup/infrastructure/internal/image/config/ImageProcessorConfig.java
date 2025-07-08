package cool.drinkup.drinkup.infrastructure.internal.image.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import cool.drinkup.drinkup.infrastructure.internal.image.config.properties.ImageProcessorProperties;
import cool.drinkup.drinkup.infrastructure.internal.image.impl.RustImageProcessor;
import cool.drinkup.drinkup.infrastructure.spi.image.ImageProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ImageProcessorConfig {

    @Bean
    public ImageProcessor rustImageProcessor(ImageProcessorProperties properties, ObjectMapper objectMapper) {
        return new RustImageProcessor(properties, objectMapper);
    }
}
