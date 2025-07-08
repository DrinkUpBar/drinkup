package cool.drinkup.drinkup.infrastructure.internal.image.impl.fal;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 工厂类，用于创建 FalImageGenerator 实例
 * 自动注入 ObjectMapper，提供多种创建方式
 */
@Component
@RequiredArgsConstructor
public class FalImageGeneratorFactory {

    private final ObjectMapper objectMapper;

    /**
     * 使用完整配置创建实例
     */
    public FalImageGenerator create(FalConfig config) {
        return FalImageGenerator.create(config, objectMapper);
    }

    /**
     * 使用 Builder 模式创建实例
     */
    public FalImageGenerator create(FalConfig.Builder configBuilder) {
        return FalImageGenerator.create(configBuilder.build(), objectMapper);
    }
}
