package cool.drinkup.drinkup.infrastructure.spi;

import io.micrometer.tracing.annotation.NewSpan;

public interface ImageCompressor {

    /**
     * Base64 图片压缩
     * @param imageUrl
     * @return
     */
    @NewSpan
    public String compress(String imageUrl);
}
