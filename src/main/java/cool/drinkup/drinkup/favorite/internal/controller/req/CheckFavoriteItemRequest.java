package cool.drinkup.drinkup.favorite.internal.controller.req;

import cool.drinkup.drinkup.favorite.spi.ObjectType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CheckFavoriteItemRequest {
    @NotNull(message = "收藏对象类型不能为空")
    private ObjectType objectType;

    @NotNull(message = "收藏对象ID不能为空")
    private Long objectId;
}
