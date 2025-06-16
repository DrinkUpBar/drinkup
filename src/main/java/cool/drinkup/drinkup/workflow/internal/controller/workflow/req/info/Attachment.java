package cool.drinkup.drinkup.workflow.internal.controller.workflow.req.info;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Attachment {

    @Builder.Default
    private List<ImageAttachment> imageAttachmentList = new ArrayList<>();

    @Builder.Default
    private List<BarAttachment> barAttachmentList = new ArrayList<>();

    @Builder.Default
    private List<MaterialAttachment> materialAttachmentList = new ArrayList<>();
}
