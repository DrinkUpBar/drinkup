package cool.drinkup.drinkup.workflow.internal.controller.workflow.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "AI材料解读请求")
public class WorkflowMaterialAnalysisReq {
    @Schema(description = "材料文本")
    private String text;
    @Schema(description = "材料列表")
    private AnalysisItem item;

    @Data
    public static class AnalysisItem {
        @Schema(description = "材料ID")
        private Long id;
        @Schema(description = "材料类型")
        @Pattern(regexp = "^(MATERIAL|BAR_STOCK)$", message = "材料类型必须是MATERIAL、BAR_STOCK")
        private String type;
    }
}
