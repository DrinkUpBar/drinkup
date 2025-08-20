package cool.drinkup.drinkup.infrastructure.internal.image.config;

import cool.drinkup.drinkup.infrastructure.internal.image.impl.DefaultImageMetadataProcessor;
import cool.drinkup.drinkup.infrastructure.spi.image.ImageMetadataProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 图片元数据处理器配置
 */
@Configuration
public class ImageMetadataConfig {

    @Bean
    public ImageMetadataProcessor imageMetadataProcessor() {
        return new DefaultImageMetadataProcessor();
    }
}
