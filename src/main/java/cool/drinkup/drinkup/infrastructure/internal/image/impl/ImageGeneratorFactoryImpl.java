package cool.drinkup.drinkup.infrastructure.internal.image.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import cool.drinkup.drinkup.infrastructure.internal.image.impl.fal.FalImageGeneratorFactory;
import cool.drinkup.drinkup.infrastructure.internal.image.impl.glif.GlifImageGeneratorFactory;
import cool.drinkup.drinkup.infrastructure.spi.image.ImageGenerator;
import cool.drinkup.drinkup.infrastructure.spi.image.config.FalConfig;
import cool.drinkup.drinkup.infrastructure.spi.image.config.GlifConfig;
import cool.drinkup.drinkup.infrastructure.spi.image.config.ImageGeneratorConfig;
import cool.drinkup.drinkup.infrastructure.spi.image.enums.ImageGeneratorTypeEnum;
import cool.drinkup.drinkup.infrastructure.spi.image.factory.ImageGeneratorFactory;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImageGeneratorFactoryImpl implements ImageGeneratorFactory {

    private final ObjectMapper objectMapper;
    private final Random random = new Random();

    @Resource
    private FalImageGeneratorFactory falImageGeneratorFactory;

    @Resource
    private GlifImageGeneratorFactory glifImageGeneratorFactory;

    @Override
    public ImageGenerator getImageGenerator(String configJson) {
        try {
            // 首先尝试解析为单个配置对象
            if (configJson.trim().startsWith("[")) {
                // 如果是数组，解析为配置列表
                List<ImageGeneratorConfig> configs =
                        objectMapper.readValue(configJson, new TypeReference<List<ImageGeneratorConfig>>() {});
                if (configs.isEmpty()) {
                    throw new IllegalArgumentException("Configuration list cannot be empty");
                }

                // 根据权重选择配置
                ImageGeneratorConfig selectedConfig = selectConfigByWeight(configs);
                log.info(
                        "Selected image generator config with type: {} and weight: {}",
                        selectedConfig.getType(),
                        selectedConfig.getWeight());
                return createImageGenerator(selectedConfig);
            } else {
                // 单个配置对象
                ImageGeneratorConfig config = objectMapper.readValue(configJson, ImageGeneratorConfig.class);
                return createImageGenerator(config);
            }
        } catch (Exception e) {
            log.error("Failed to parse image generator config: {}", e.getMessage());
            throw new IllegalArgumentException("Failed to parse image generator config: " + e.getMessage());
        }
    }

    /**
     * 根据权重选择配置
     * 使用加权随机算法，权重越高的配置被选中的概率越大
     */
    private ImageGeneratorConfig selectConfigByWeight(List<ImageGeneratorConfig> configs) {
        // 计算总权重
        double totalWeight = configs.stream()
                .mapToDouble(config -> config.getWeight() != null ? config.getWeight() : 1.0)
                .sum();

        if (totalWeight <= 0) {
            // 如果所有权重都为0或负数，随机选择一个
            return configs.get(random.nextInt(configs.size()));
        }

        // 生成随机数
        double randomValue = random.nextDouble() * totalWeight;

        // 根据权重区间选择配置
        double currentWeight = 0;
        for (ImageGeneratorConfig config : configs) {
            double weight = config.getWeight() != null ? config.getWeight() : 1.0;
            currentWeight += weight;
            if (randomValue <= currentWeight) {
                return config;
            }
        }

        // 兜底：返回最后一个配置
        return configs.get(configs.size() - 1);
    }

    /**
     * 根据配置创建 ImageGenerator
     */
    private ImageGenerator createImageGenerator(ImageGeneratorConfig config) {
        String type = config.getType();
        ImageGeneratorTypeEnum typeEnum = ImageGeneratorTypeEnum.fromString(type);
        Object configObj = config.getConfig();

        switch (typeEnum) {
            case FAL:
                return falImageGeneratorFactory.create(objectMapper.convertValue(configObj, FalConfig.class));
            case GLIF:
                return glifImageGeneratorFactory.create(objectMapper.convertValue(configObj, GlifConfig.class));
            default:
                throw new IllegalArgumentException("Unsupported image generator type: " + type);
        }
    }
}
