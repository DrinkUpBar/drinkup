package cool.drinkup.drinkup.infrastructure.internal.image.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "image.processor")
public class ImageProcessorProperties {
    private String apiHost = "http://localhost:3000";
}
