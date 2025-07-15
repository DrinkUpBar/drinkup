package cool.drinkup.drinkup.infrastructure.spi.image.config;

import lombok.Data;

@Data
public class ImageGeneratorConfig {
    private String type;
    private Object config;
}
