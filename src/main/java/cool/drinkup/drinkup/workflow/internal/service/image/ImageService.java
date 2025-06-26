package cool.drinkup.drinkup.workflow.internal.service.image;

import cool.drinkup.drinkup.infrastructure.spi.ImageCompressor;
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

    private final RestClient restClient;
    private static String prefix = "images/";

    @Value("${drinkup.image.save.s3.url:https://img.fjhdream.lol/}")
    private String imageUrl;

    @Value("${drinkup.image.save.s3.internal.url:https://img.fjhdream.lol/}")
    private String imageInternalUrl;

    @Value("${drinkup.image.save.s3.bucket:object-bucket}")
    private String bucket;

    public ImageService(S3Client s3Client, ImageCompressor imageCompressor) {
        this.s3Client = s3Client;
        this.imageCompressor = imageCompressor;

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
            // Upload the file to S3
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(getContentTypeFromExtension(extension))
                    .build();

            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromInputStream(new java.io.ByteArrayInputStream(imageBytes), imageBytes.length));

            log.info("Stored image with ID: {} in S3 bucket: {} with key: {}", imageId, bucket, key);
            return filename;
        } catch (Exception e) {
            log.error("Failed to store image from URL: {}", imageUrl, e);
            throw new RuntimeException("Failed to store image: " + e.getMessage(), e);
        }
    }

    private byte[] downloadImageWithRetry(String imageUrl) {
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
