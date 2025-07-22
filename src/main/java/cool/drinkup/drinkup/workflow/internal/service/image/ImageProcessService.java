package cool.drinkup.drinkup.workflow.internal.service.image;

import cool.drinkup.drinkup.infrastructure.spi.image.ImageProcessor;
import cool.drinkup.drinkup.shared.spi.ImageProcessServiceFacade;
import io.micrometer.observation.annotation.Observed;
import org.springframework.stereotype.Service;

@Service
public class ImageProcessService implements ImageProcessServiceFacade {

    private final ImageProcessor imageProcessor;
    private final ImageService imageService;

    public ImageProcessService(ImageProcessor imageProcessor, ImageService imageService) {
        this.imageProcessor = imageProcessor;
        this.imageService = imageService;
    }

    /**
     * 移除图片背景
     * @param imageUrl
     * @return
     */
    @Observed(
            name = "image.removeBackground",
            contextualName = "移除图片背景",
            lowCardinalityKeyValues = {
                "Tag", "image",
                "Server", "imageProcessor"
            })
    @Override
    public String removeBackground(String imageUrl) {
        byte[] imageBytes = imageService.downloadImageWithRetry(imageUrl);
        String imageBase64 = java.util.Base64.getEncoder().encodeToString(imageBytes);
        String processedImageBase64 = imageProcessor.removeBackground(imageBase64);
        String processedImageId = imageService.storeImageBase64(processedImageBase64);
        return imageService.getImageUrl(processedImageId);
    }

    /**
     * 移除图片背景并返回图片ID
     * @param imageUrl
     * @return imageId   图片ID
     */
    @Observed(
            name = "image.removeBackground",
            contextualName = "移除图片背景",
            lowCardinalityKeyValues = {
                "Tag", "image",
                "Server", "imageProcessor"
            })
    @Override
    public String removeBackgroundReturnImageId(String imageUrl) {
        byte[] imageBytes = imageService.downloadImageWithRetry(imageUrl);
        String imageBase64 = java.util.Base64.getEncoder().encodeToString(imageBytes);
        String processedImageBase64 = imageProcessor.removeBackground(imageBase64);
        String processedImageId = imageService.storeImageBase64(processedImageBase64);
        return processedImageId;
    }
}
