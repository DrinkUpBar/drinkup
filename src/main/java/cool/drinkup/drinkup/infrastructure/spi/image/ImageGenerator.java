package cool.drinkup.drinkup.infrastructure.spi.image;

import io.micrometer.tracing.annotation.NewSpan;

public interface ImageGenerator {
    /**
     * 生成图片
     * @param prompt 提示词
     * @return 图片URL
     */
    @NewSpan
    public String generateImage(String prompt);
}
