package cool.drinkup.drinkup.record.internal.controller.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;

@Data
@Schema(description = "品鉴记录响应")
public class TastingRecordResp {

    @Schema(description = "品鉴记录ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "饮品ID")
    private Long beverageId;

    @Schema(description = "饮品类型")
    private String beverageType;

    @Schema(description = "品鉴内容")
    private String content;

    @Schema(description = "品鉴时间（格式：yyyy-MM-dd HH:mm:ss）")
    private String tastingDate;

    @Schema(description = "创建时间（格式：yyyy-MM-dd HH:mm:ss）")
    private String createdTime;

    @Schema(description = "图片信息列表")
    private List<TastingRecordImageResp> images;
}
