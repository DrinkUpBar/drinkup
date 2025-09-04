package cool.drinkup.drinkup.favorite.internal.controller.req;

import cool.drinkup.drinkup.favorite.spi.ObjectType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "添加收藏请求参数")
@Data
public class AddFavoriteRequest {
    @Schema(description = "收藏对象类型", required = true)
    @NotNull
    private ObjectType objectType;

    @Schema(description = "收藏对象ID", required = true)
    @NotNull
    private Long objectId;

    @Schema(description = "收藏备注（可选）")
    private String note;
}
