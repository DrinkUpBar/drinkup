package cool.drinkup.drinkup.workflow.internal.controller.workflow.req;

import lombok.Data;

@Data
public class WorkflowStockRecognitionReq {
    private Long barId;
    private String imageId;
}
