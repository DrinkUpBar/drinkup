package cool.drinkup.drinkup.workflow.internal.service.image;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import cool.drinkup.drinkup.infrastructure.spi.ImageGenerator;
import cool.drinkup.drinkup.workflow.internal.exception.RetryException;
import io.micrometer.observation.annotation.Observed;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ImageGenerateService {

    private final ImageGenerator glifImageGenerator;

    private final ImageGenerator falImageGenerator;

    public ImageGenerateService(@Qualifier("glifImageGenerator") ImageGenerator glifImageGenerator,
            @Qualifier("falImageGenerator") ImageGenerator falImageGenerator) {
        this.glifImageGenerator = glifImageGenerator;
        this.falImageGenerator = falImageGenerator;
    }

    @Retryable(value = {
            RetryException.class }, maxAttempts = 1, backoff = @Backoff(delay = 1000))
    @Observed(name = "image.generate", contextualName = "生成图片", lowCardinalityKeyValues = {
            "Tag", "image",
            "Server", "glif"
    })
    public String generateImage(String prompt) {
        try {
            return glifImageGenerator.generateImage(prompt);
        } catch (Exception e) {
            throw new RetryException(e.getMessage());
        }
    }

    @Observed(name = "image.generate", contextualName = "生成图片", lowCardinalityKeyValues = {
            "Tag", "image",
            "Server", "fal"
    })
    @Recover
    public String recover(RetryException e, String prompt) {
        log.warn("glif generate image failed, use fal to generate image");
        return falImageGenerator.generateImage(prompt);
    }

}
