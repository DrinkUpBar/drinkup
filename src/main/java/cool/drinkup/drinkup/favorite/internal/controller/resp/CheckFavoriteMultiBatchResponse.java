package cool.drinkup.drinkup.favorite.internal.controller.resp;

import cool.drinkup.drinkup.favorite.spi.FavoriteType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import lombok.Data;

@Data
@Schema(description = "多类型批量检查收藏状态响应")
public class CheckFavoriteMultiBatchResponse {
    @Schema(description = "收藏状态Map，key为收藏类型，value为该类型下的收藏状态Map")
    private Map<FavoriteType, Map<Long, Boolean>> statusMap;
}
