package cool.drinkup.drinkup.favorite.internal.controller.resp;

import cool.drinkup.drinkup.favorite.spi.ObjectType;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckMadeMultiBatchResponse {
    private Map<ObjectType, Map<Long, Boolean>> result;
}
