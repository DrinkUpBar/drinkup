package cool.drinkup.drinkup.record.internal.controller.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "更新品鉴记录请求")
public class UpdateTastingRecordRequest {

    @Schema(description = "品鉴内容")
    private String content;
}
