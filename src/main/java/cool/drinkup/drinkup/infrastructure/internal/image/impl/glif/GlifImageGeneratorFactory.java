package cool.drinkup.drinkup.infrastructure.internal.image.impl.glif;

import com.fasterxml.jackson.databind.ObjectMapper;
import cool.drinkup.drinkup.infrastructure.spi.image.config.GlifConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 工厂类，用于创建 GlifImageGenerator 实例
 * 自动注入 ObjectMapper，提供多种创建方式
 */
@Component
@RequiredArgsConstructor
public class GlifImageGeneratorFactory {

    private final ObjectMapper objectMapper;

    /**
     * 使用完整配置创建实例
     */
    public GlifImageGenerator create(GlifConfig config) {
        return GlifImageGenerator.create(config, objectMapper);
    }

    /**
     * 使用 Builder 模式创建实例
     */
    public GlifImageGenerator create(GlifConfig.Builder configBuilder) {
        return GlifImageGenerator.create(configBuilder.build(), objectMapper);
    }
}
