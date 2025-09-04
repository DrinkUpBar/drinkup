package cool.drinkup.drinkup.workflow.internal.controller.workflow.req;

import cool.drinkup.drinkup.workflow.internal.controller.workflow.req.info.Attachment;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class WorkflowBartenderChatV2Req {
    private String conversationId;

    @Pattern(regexp = "^(PHILOSOPHY|CYBER_WORK|MOVIE|RANDOM)?$", message = "主题必须是有效的ThemeEnum值")
    private String theme;

    private String userDemand;
    private Attachment attachment = new Attachment();
}
