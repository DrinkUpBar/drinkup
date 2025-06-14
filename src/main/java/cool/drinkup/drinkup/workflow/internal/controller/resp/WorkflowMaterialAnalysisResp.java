package cool.drinkup.drinkup.workflow.internal.controller.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "AI材料解读响应")
public class WorkflowMaterialAnalysisResp {
    @Schema(description = "材料描述")
    private String description;

}