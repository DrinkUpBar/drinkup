package cool.drinkup.drinkup.infrastructure.spi.image;

import cool.drinkup.drinkup.infrastructure.spi.image.dto.ImageMetadata;
import java.io.IOException;

/**
 * 图片元数据处理器接口
 * 用于为图片添加EXIF和XMP元数据信息
 */
public interface ImageMetadataProcessor {

    /**
     * 为图片字节数组添加元数据信息
     * @param imageBytes 原始图片字节数组
     * @param metadata 要添加的元数据信息
     * @return 带有元数据的图片字节数组
     * @throws IOException 处理过程中的IO异常
     */
    byte[] addMetadata(byte[] imageBytes, ImageMetadata metadata) throws IOException;

    /**
     * 为Base64编码的图片添加元数据信息
     * @param imageBase64 Base64编码的图片数据
     * @param metadata 要添加的元数据信息
     * @return 带有元数据的Base64编码图片数据
     * @throws IOException 处理过程中的IO异常
     */
    String addMetadataToBase64(String imageBase64, ImageMetadata metadata) throws IOException;
}
