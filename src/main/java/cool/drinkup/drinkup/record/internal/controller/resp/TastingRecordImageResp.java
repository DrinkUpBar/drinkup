package cool.drinkup.drinkup.record.internal.controller.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "品鉴记录图片响应")
public class TastingRecordImageResp {

    @Schema(description = "图片URL")
    private String url;

    @Schema(description = "排序序号")
    private Integer sort;

    @Schema(description = "是否为封面图片（1：是，0：否）")
    private Integer isCover;
}
