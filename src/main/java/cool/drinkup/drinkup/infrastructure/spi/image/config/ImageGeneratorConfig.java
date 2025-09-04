package cool.drinkup.drinkup.infrastructure.spi.image.config;

import lombok.Data;

@Data
public class ImageGeneratorConfig {
    private String type;
    private Object config;

    /**
     * 权重，用于在多个配置中进行选择，权重越高越容易被选中
     * 默认权重为 1.0
     */
    private Double weight = 1.0;
}
