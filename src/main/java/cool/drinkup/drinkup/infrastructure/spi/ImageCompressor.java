package cool.drinkup.drinkup.infrastructure.spi;

import io.micrometer.tracing.annotation.NewSpan;

public interface ImageCompressor {

    @NewSpan
    public String compress(String imageUrl);
}
