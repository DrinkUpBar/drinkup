package cool.drinkup.drinkup.infrastructure.internal.image.config;

import cool.drinkup.drinkup.infrastructure.internal.image.config.properties.ImgProxyProperties;
import cool.drinkup.drinkup.infrastructure.internal.image.impl.ImgProxyImageCompressor;
import cool.drinkup.drinkup.infrastructure.spi.ImageCompressor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ImageCompressorConfig {

    @Bean
    public ImageCompressor imgProxyImageCompressor(ImgProxyProperties properties) {
        return new ImgProxyImageCompressor(properties);
    }
}
