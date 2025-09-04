package cool.drinkup.drinkup.workflow.internal.controller.workflow.req.info;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageAttachment {
    private String imageId;
    private String imageBase64;
    private String mimeType;
}
