package cool.drinkup.drinkup.favorite.internal.controller.req;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckMadeMultiBatchRequest {
    private List<CheckFavoriteItemRequest> items;
}
