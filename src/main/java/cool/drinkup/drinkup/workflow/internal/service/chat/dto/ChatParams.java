package cool.drinkup.drinkup.workflow.internal.service.chat.dto;


import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatParams {
    private String userStock;
    private String imageId;
    private List<ImageAttachment> imageAttachmentList;

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Data
    public static class ImageAttachment {
        private String imageId;
        private String mode;
    }
}
