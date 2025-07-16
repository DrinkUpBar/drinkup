package cool.drinkup.drinkup.workflow.internal.service.image;

import cool.drinkup.drinkup.infrastructure.spi.image.ImageGenerator;
import cool.drinkup.drinkup.infrastructure.spi.image.factory.ImageGeneratorFactory;
import cool.drinkup.drinkup.workflow.internal.exception.RetryException;
import cool.drinkup.drinkup.workflow.internal.service.theme.ThemeEnum;
import cool.drinkup.drinkup.workflow.internal.service.theme.ThemeFactory;
import io.micrometer.observation.annotation.Observed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ImageGenerateService {

    private final ImageGenerator glifImageGenerator;

    private final ImageGenerator falImageGenerator;

    private final ThemeFactory themeFactory;

    private final ImageGeneratorFactory imageGeneratorFactory;

    public ImageGenerateService(
            @Qualifier("glifImageGenerator") ImageGenerator glifImageGenerator,
            @Qualifier("falImageGenerator") ImageGenerator falImageGenerator,
            ThemeFactory themeFactory,
            ImageGeneratorFactory imageGeneratorFactory) {
        this.glifImageGenerator = glifImageGenerator;
        this.falImageGenerator = falImageGenerator;
        this.themeFactory = themeFactory;
        this.imageGeneratorFactory = imageGeneratorFactory;
    }

    @Retryable(
            value = {RetryException.class},
            maxAttempts = 1,
            backoff = @Backoff(delay = 1000))
    @Observed(
            name = "image.generate",
            contextualName = "生成图片",
            lowCardinalityKeyValues = {"Tag", "image"})
    public String generateImage(String prompt, ThemeEnum themeEnum) {
        try {
            var theme = themeFactory.getTheme(themeEnum);
            var themeImageConfig = theme.getThemeImageConfig();
            var imageGenerator = imageGeneratorFactory.getImageGenerator(themeImageConfig);
            return imageGenerator.generateImage(prompt);
        } catch (Exception e) {
            log.error("[IMAGE] generate image failed, use glif to generate image", e);
            throw new RetryException(e.getMessage());
        }
    }

    @Observed(
            name = "image.generate",
            contextualName = "生成图片",
            lowCardinalityKeyValues = {"Tag", "image"})
    @Recover
    public String recover(RetryException e, String prompt, ThemeEnum themeEnum) {
        return glifImageGenerator.generateImage(prompt);
    }
}
