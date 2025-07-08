package cool.drinkup.drinkup.infrastructure.spi.image.factory;

import cool.drinkup.drinkup.infrastructure.spi.image.ImageGenerator;
import cool.drinkup.drinkup.infrastructure.spi.image.enums.ImageGeneratorTypeEnum;

public interface ImageGeneratorFactory {

    ImageGenerator getImageGenerator(ImageGeneratorTypeEnum type, String configJson);
}
