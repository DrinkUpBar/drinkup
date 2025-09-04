package cool.drinkup.drinkup.workflow.internal.controller.workflow.req;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "AI翻译请求")
public class WorkflowTranslateReq {

    @NotNull(message = "翻译文本不能为空")
    @Size(max = 5000, message = "翻译文本长度不能超过5000字符")
    @Schema(description = "待翻译的文本", example = "金酒")
    private String text;
}
