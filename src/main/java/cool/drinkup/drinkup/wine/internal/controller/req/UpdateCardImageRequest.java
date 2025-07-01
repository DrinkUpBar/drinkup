package cool.drinkup.drinkup.wine.internal.controller.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "更新卡片图片请求")
public class UpdateCardImageRequest {

    @Schema(description = "卡片图片ID", example = "abc123.jpg")
    private String cardImage;
}
