package cool.drinkup.drinkup.record.internal.controller.req;

import cool.drinkup.drinkup.shared.spi.BeverageTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
@Schema(description = "添加品鉴记录请求")
public class AddTastingRecordRequest {

    @NotNull(message = "饮品ID不能为空")
    @Schema(description = "饮品ID")
    private Long beverageId;

    @NotNull(message = "饮品类型不能为空")
    @Schema(description = "饮品类型")
    private BeverageTypeEnum beverageType;

    @Schema(description = "品鉴内容")
    private String content;

    @Schema(description = "图片URL列表")
    private List<String> images;
}
