package cool.drinkup.drinkup.infrastructure.spi.image.enums;

public enum ImageGeneratorTypeEnum {
    FAL,
    GLIF,
    ;

    public static ImageGeneratorTypeEnum fromString(String type) {
        return ImageGeneratorTypeEnum.valueOf(type.toUpperCase());
    }
}
