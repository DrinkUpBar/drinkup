package cool.drinkup.drinkup.wine.internal.controller;

import com.fasterxml.jackson.annotation.JsonIgnore;
import cool.drinkup.drinkup.wine.internal.enums.PropagateTypeEnum;
import lombok.Data;

@Data
public class PropagateRequest {
    private PropagateTypeEnum type;
    private Long recordId;

    @JsonIgnore
    private Long userId;
}
