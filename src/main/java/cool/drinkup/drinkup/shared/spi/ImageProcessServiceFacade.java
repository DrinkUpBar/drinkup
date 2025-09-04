package cool.drinkup.drinkup.shared.spi;

public interface ImageProcessServiceFacade {
    /**
     * 移除图片背景
     * @param imageUrl
     * @return 图片Url
     */
    String removeBackground(String imageUrl);

    /**
     * 移除图片背景并返回图片ID
     * @param imageUrl
     * @return imageId
     */
    String removeBackgroundReturnImageId(String imageUrl);
}
