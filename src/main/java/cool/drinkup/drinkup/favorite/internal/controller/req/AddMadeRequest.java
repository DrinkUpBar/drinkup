package cool.drinkup.drinkup.favorite.internal.controller.req;

import cool.drinkup.drinkup.favorite.spi.ObjectType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddMadeRequest {
    @NotNull
    private ObjectType objectType;

    @NotNull
    private Long objectId;
}
