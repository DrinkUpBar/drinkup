package cool.drinkup.drinkup.workflow.internal.controller.workflow.req;

import jakarta.validation.constraints.Pattern;
import java.util.List;
import lombok.Data;

@Data
public class WorkflowBartenderChatReq {
    private List<WorkflowBartenderChatVo> messages;

    @Pattern(regexp = "^(PHILOSOPHY|CYBER_WORK|MOVIE|RANDOM)?$", message = "主题必须是有效的ThemeEnum值")
    private String theme;

    private String userDemand;
    private List<Long> barIds;

    @Data
    public static class WorkflowBartenderChatVo {
        @Pattern(regexp = "^(assistant|user)$", message = "角色只能是 assistant 或 user")
        private String role;

        private String content;
    }
}
