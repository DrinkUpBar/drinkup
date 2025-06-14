package cool.drinkup.drinkup.workflow.internal.controller.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "AI材料解读请求")
public class WorkflowMaterialAnalysisReq {
    @Schema(description = "材料ID")
    private Long materialId;
}
