package cool.drinkup.drinkup.wine.internal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImageProcessingTaskDto {
    private String taskId;
    private String entityType;
    private Long entityId;
    private String imageId;
    private Integer retryCount = 0;
    private String errorMessage;
    private Long timestamp;

    public ImageProcessingTaskDto(String entityType, Long entityId, String imageId) {
        this.taskId = java.util.UUID.randomUUID().toString();
        this.entityType = entityType;
        this.entityId = entityId;
        this.imageId = imageId;
        this.timestamp = System.currentTimeMillis();
    }

    public enum EntityType {
        USER_WINE,
        WINE
    }
}
