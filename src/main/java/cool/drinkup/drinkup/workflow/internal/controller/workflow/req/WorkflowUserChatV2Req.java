package cool.drinkup.drinkup.workflow.internal.controller.workflow.req;

import cool.drinkup.drinkup.workflow.internal.controller.workflow.req.info.Attachment;
import lombok.Data;

@Data
public class WorkflowUserChatV2Req {
    private String conversationId;
    private String userMessage;
    private Attachment attachment = new Attachment();
}
