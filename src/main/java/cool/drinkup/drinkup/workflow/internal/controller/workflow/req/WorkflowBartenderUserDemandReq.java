package cool.drinkup.drinkup.workflow.internal.controller.workflow.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Public bartender chat request with only user demand and user id.
 */
@Data
public class WorkflowBartenderUserDemandReq {
    private String userDemand;

    @JsonProperty("user_id")
    private Long userId;
}
