package cool.drinkup.drinkup.workflow.internal.service.image;

import cool.drinkup.drinkup.infrastructure.spi.image.ImageCompressor;
import cool.drinkup.drinkup.infrastructure.spi.image.ImageMetadataProcessor;
import cool.drinkup.drinkup.infrastructure.spi.image.dto.ImageMetadata;
import cool.drinkup.drinkup.shared.spi.ImageServiceFacade;
import java.io.IOException;
import java.net.URI;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
@Service
public class ImageService implements ImageServiceFacade {

    private final S3Client s3Client;
    private final ImageCompressor imageCompressor;
    private final ImageMetadataProcessor imageMetadataProcessor;

    private final RestClient restClient;
    private static String prefix = "images/";

    @Value("${drinkup.image.save.s3.url:https://img.fjhdream.lol/}")
    private String imageUrl;

    @Value("${drinkup.image.save.s3.internal.url:https://img.fjhdream.lol/}")
    private String imageInternalUrl;

    @Value("${drinkup.image.save.s3.bucket:object-bucket}")
    private String bucket;

    public ImageService(
            S3Client s3Client, ImageCompressor imageCompressor, ImageMetadataProcessor imageMetadataProcessor) {
        this.s3Client = s3Client;
        this.imageCompressor = imageCompressor;
        this.imageMetadataProcessor = imageMetadataProcessor;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(30000); // 30 seconds
        factory.setReadTimeout(30000); // 30 seconds

        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    public String storeImage(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Cannot store empty file");
            }

            // Generate a unique ID for the image
            String imageId = UUID.randomUUID().toString();

            // Get file extension
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            // Create the final filename with ID
            String filename = imageId + extension;
            String key = prefix + filename;

            // Upload the file to S3
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            log.info("Stored image with ID: {} in S3 bucket: {} with key: {}", imageId, bucket, key);
            return filename;
        } catch (IOException e) {
            log.error("Failed to store image", e);
            throw new RuntimeException("Failed to store image: " + e.getMessage(), e);
        }
    }

    public String storeImage(String imageUrl) {
        log.info("Storing image from URL: {}", imageUrl);
        byte[] imageBytes = downloadImageWithRetry(imageUrl);

        // Extract extension from original URL
        String extension = ".jpg"; // Default extension
        if (imageUrl.contains(".")) {
            String urlPath = URI.create(imageUrl).getPath();
            int lastDotIndex = urlPath.lastIndexOf(".");
            if (lastDotIndex > 0) {
                extension = urlPath.substring(lastDotIndex);
            }
        }

        String imageId = UUID.randomUUID().toString();
        String filename = imageId + extension;
        String key = prefix + filename;

        try {
            // 添加默认的DrinkUp元数据
            ImageMetadata metadata = ImageMetadata.createDrinkUpMetadata(
                    "Relaxed",
                    "Grapefruit, Herbal",
                    "AI-generated cocktail card for mood: Calm Summer Night",
                    "AI, cocktail, qwen, emotional-drink, grapefruit");

            // 为图片添加元数据
            byte[] processedImageBytes = imageMetadataProcessor.addMetadata(imageBytes, metadata);

            // Upload the file to S3
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(getContentTypeFromExtension(extension))
                    .build();

            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromInputStream(
                            new java.io.ByteArrayInputStream(processedImageBytes), processedImageBytes.length));

            log.info(
                    "Stored image with ID: {} in S3 bucket: {} with key: {}, with metadata added",
                    imageId,
                    bucket,
                    key);
            return filename;
        } catch (Exception e) {
            log.error("Failed to store image from URL: {}", imageUrl, e);
            throw new RuntimeException("Failed to store image: " + e.getMessage(), e);
        }
    }

    @Override
    public String storeImageBase64(String imageBase64) {
        String imageId = UUID.randomUUID().toString();

        // 检测图片格式并获取相应的扩展名和内容类型
        ImageFormatInfo formatInfo = detectImageFormatFromBase64(imageBase64);
        String filename = imageId + formatInfo.extension;
        String key = prefix + filename;

        try {
            // 解码图片数据
            byte[] imageBytes = java.util.Base64.getDecoder().decode(imageBase64);

            // 添加默认的DrinkUp元数据
            ImageMetadata metadata = ImageMetadata.createDrinkUpMetadata(
                    "Relaxed",
                    "Grapefruit, Herbal",
                    "AI-generated cocktail card for mood: Calm Summer Night",
                    "AI, cocktail, qwen, emotional-drink, grapefruit");

            // 为图片添加元数据
            byte[] processedImageBytes = imageMetadataProcessor.addMetadata(imageBytes, metadata);

            // Upload the file to S3
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(formatInfo.contentType)
                    .build();

            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromInputStream(
                            new java.io.ByteArrayInputStream(processedImageBytes), processedImageBytes.length));

            log.info(
                    "Stored image with ID: {} in S3 bucket: {} with key: {}, with metadata added",
                    imageId,
                    bucket,
                    key);
            return filename;
        } catch (Exception e) {
            log.error("Failed to store image from base64 data", e);
            throw new RuntimeException("Failed to store image: " + e.getMessage(), e);
        }
    }

    private ImageFormatInfo detectImageFormatFromBase64(String imageBase64) {
        try {
            byte[] imageBytes = java.util.Base64.getDecoder().decode(imageBase64);
            return detectImageFormat(imageBytes);
        } catch (Exception e) {
            log.warn("Failed to detect image format from base64, using default JPEG format", e);
            return new ImageFormatInfo(".jpg", "image/jpeg");
        }
    }

    private ImageFormatInfo detectImageFormat(byte[] imageBytes) {
        if (imageBytes.length < 8) {
            return new ImageFormatInfo(".jpg", "image/jpeg");
        }

        // JPEG: FF D8 FF
        if (imageBytes[0] == (byte) 0xFF && imageBytes[1] == (byte) 0xD8 && imageBytes[2] == (byte) 0xFF) {
            return new ImageFormatInfo(".jpg", "image/jpeg");
        }

        // PNG: 89 50 4E 47 0D 0A 1A 0A
        if (imageBytes[0] == (byte) 0x89
                && imageBytes[1] == 0x50
                && imageBytes[2] == 0x4E
                && imageBytes[3] == 0x47
                && imageBytes[4] == 0x0D
                && imageBytes[5] == 0x0A
                && imageBytes[6] == 0x1A
                && imageBytes[7] == 0x0A) {
            return new ImageFormatInfo(".png", "image/png");
        }

        // GIF: 47 49 46 38 (GIF8)
        if (imageBytes[0] == 0x47 && imageBytes[1] == 0x49 && imageBytes[2] == 0x46 && imageBytes[3] == 0x38) {
            return new ImageFormatInfo(".gif", "image/gif");
        }

        // WebP: 52 49 46 46 ... 57 45 42 50 (RIFF...WEBP)
        if (imageBytes.length >= 12
                && imageBytes[0] == 0x52
                && imageBytes[1] == 0x49
                && imageBytes[2] == 0x46
                && imageBytes[3] == 0x46
                && imageBytes[8] == 0x57
                && imageBytes[9] == 0x45
                && imageBytes[10] == 0x42
                && imageBytes[11] == 0x50) {
            return new ImageFormatInfo(".webp", "image/webp");
        }

        // BMP: 42 4D
        if (imageBytes[0] == 0x42 && imageBytes[1] == 0x4D) {
            return new ImageFormatInfo(".bmp", "image/bmp");
        }

        // 默认返回 JPEG
        log.warn("Unknown image format, using default JPEG format");
        return new ImageFormatInfo(".jpg", "image/jpeg");
    }

    private static class ImageFormatInfo {
        final String extension;
        final String contentType;

        ImageFormatInfo(String extension, String contentType) {
            this.extension = extension;
            this.contentType = contentType;
        }
    }

    public byte[] downloadImageWithRetry(String imageUrl) {
        int maxRetries = 3;
        int retryDelayMs = 1000; // 1 second initial delay

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                byte[] imageBytes =
                        restClient.get().uri(URI.create(imageUrl)).retrieve().body(byte[].class);

                if (imageBytes == null || imageBytes.length == 0) {
                    throw new RuntimeException("Empty response");
                }

                return imageBytes;
            } catch (Exception e) {
                if (attempt == maxRetries) {
                    log.error("Failed to download image after {} attempts from URL: {}", maxRetries, imageUrl, e);
                    throw new RuntimeException("Failed to download image: " + e.getMessage(), e);
                }

                log.warn(
                        "Error downloading image (attempt {}/{}): {}. Retrying in {} ms...",
                        attempt,
                        maxRetries,
                        e.getMessage(),
                        retryDelayMs);

                try {
                    Thread.sleep(retryDelayMs);
                    // Exponential backoff
                    retryDelayMs *= 2;
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Download interrupted", ie);
                }
            }
        }

        // This should never happen due to the throw in the final retry
        throw new RuntimeException("Failed to download image after retries");
    }

    private String getContentTypeFromExtension(String extension) {
        return switch (extension.toLowerCase()) {
            case ".jpg", ".jpeg" -> "image/jpeg";
            case ".png" -> "image/png";
            case ".gif" -> "image/gif";
            case ".webp" -> "image/webp";
            case ".svg" -> "image/svg+xml";
            case ".bmp" -> "image/bmp";
            default -> "image/jpeg";
        };
    }

    public Resource loadImage(String imageId) {
        String imageUrl = getInternalImageUrl(imageId);
        String compressedImageUrl = imageCompressor.compress(imageUrl);
        try {
            byte[] imageBytes = downloadImageWithRetry(compressedImageUrl);
            log.info("Successfully loaded image: {}", imageId);
            return new ByteArrayResource(imageBytes);
        } catch (Exception e) {
            log.error("Failed to load image: {}", imageId, e);
            throw new RuntimeException("Failed to load image: " + e.getMessage(), e);
        }
    }

    @Override
    public String getImageUrl(String imageId) {
        if (!StringUtils.hasText(imageId)) {
            return null;
        }
        return imageUrl + prefix + imageId;
    }

    private String getInternalImageUrl(String imageId) {
        if (!StringUtils.hasText(imageId)) {
            return null;
        }
        return imageInternalUrl + prefix + imageId;
    }
}
