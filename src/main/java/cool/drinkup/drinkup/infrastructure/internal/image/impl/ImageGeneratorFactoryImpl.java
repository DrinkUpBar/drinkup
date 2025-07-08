package cool.drinkup.drinkup.infrastructure.internal.image.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import cool.drinkup.drinkup.infrastructure.internal.image.impl.fal.FalConfig;
import cool.drinkup.drinkup.infrastructure.internal.image.impl.fal.FalImageGeneratorFactory;
import cool.drinkup.drinkup.infrastructure.internal.image.impl.glif.GlifConfig;
import cool.drinkup.drinkup.infrastructure.internal.image.impl.glif.GlifImageGeneratorFactory;
import cool.drinkup.drinkup.infrastructure.spi.image.ImageGenerator;
import cool.drinkup.drinkup.infrastructure.spi.image.enums.ImageGeneratorTypeEnum;
import cool.drinkup.drinkup.infrastructure.spi.image.factory.ImageGeneratorFactory;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImageGeneratorFactoryImpl implements ImageGeneratorFactory {

    private final ObjectMapper objectMapper;

    @Resource
    private FalImageGeneratorFactory falImageGeneratorFactory;

    @Resource
    private GlifImageGeneratorFactory glifImageGeneratorFactory;

    @Override
    public ImageGenerator getImageGenerator(ImageGeneratorTypeEnum type, String configJson) {
        try {
            switch (type) {
                case FAL:
                    return falImageGeneratorFactory.create(objectMapper.readValue(configJson, FalConfig.class));
                case GLIF:
                    return glifImageGeneratorFactory.create(objectMapper.readValue(configJson, GlifConfig.class));
                default:
                    throw new IllegalArgumentException("Unsupported image generator type: " + type);
            }
        } catch (Exception e) {
            log.error("Failed to parse image generator config: {}", e.getMessage());
            throw new IllegalArgumentException("Failed to parse image generator config: " + e.getMessage());
        }
    }
}
