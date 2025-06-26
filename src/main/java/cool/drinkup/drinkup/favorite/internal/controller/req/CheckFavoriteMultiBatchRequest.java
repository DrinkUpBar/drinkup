package cool.drinkup.drinkup.favorite.internal.controller.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Data;

@Data
@Schema(description = "批量检查收藏状态请求")
public class CheckFavoriteMultiBatchRequest {
    @Schema(description = "收藏对象列表")
    @NotEmpty(message = "收藏对象列表不能为空")
    @Valid
    private List<CheckFavoriteItemRequest> items;
}
