package cool.drinkup.drinkup.shared.spi;

import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ImageServiceMapper {
    
    private final ImageServiceFacade imageServiceFacade;
    
    @Named("imageToUrl")
    public String imageToUrl(String imageId) {
        return imageServiceFacade.getImageUrl(imageId);
    }
} 