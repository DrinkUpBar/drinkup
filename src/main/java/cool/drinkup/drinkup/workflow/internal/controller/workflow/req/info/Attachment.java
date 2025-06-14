package cool.drinkup.drinkup.workflow.internal.controller.workflow.req.info;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class Attachment {
    private List<ImageAttachment> imageAttachmentList = new ArrayList<>();

    private List<BarAttachment> barAttachmentList = new ArrayList<>();

    private List<MaterialAttachment> materialAttachmentList = new ArrayList<>();
}
