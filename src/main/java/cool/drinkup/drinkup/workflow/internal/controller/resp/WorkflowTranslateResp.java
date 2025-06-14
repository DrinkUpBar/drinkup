package cool.drinkup.drinkup.workflow.internal.controller.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "AI翻译响应")
public class WorkflowTranslateResp {

    @Schema(description = "翻译结果")
    private String translatedText;

}