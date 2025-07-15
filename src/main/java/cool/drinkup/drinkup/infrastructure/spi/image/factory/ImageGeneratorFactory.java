package cool.drinkup.drinkup.infrastructure.spi.image.factory;

import cool.drinkup.drinkup.infrastructure.spi.image.ImageGenerator;

public interface ImageGeneratorFactory {

    ImageGenerator getImageGenerator(String configJson);
}
