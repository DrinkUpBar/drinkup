package cool.drinkup.drinkup.shared.spi;

import org.springframework.modulith.NamedInterface;

@NamedInterface("spi")
public interface ImageServiceFacade {
    String getImageUrl(String imageId);

    String storeImageBase64(String base64Image);
}
