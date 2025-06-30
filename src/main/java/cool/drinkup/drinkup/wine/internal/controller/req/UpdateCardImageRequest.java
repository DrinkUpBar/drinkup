package cool.drinkup.drinkup.wine.internal.controller.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "更新卡片图片请求")
public class UpdateCardImageRequest {

    @NotBlank(message = "卡片图片ID不能为空")
    @Schema(description = "卡片图片ID", example = "abc123.jpg")
    private String cardImage;
}
