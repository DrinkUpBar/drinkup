package cool.drinkup.drinkup.favorite.internal.dto;

import cool.drinkup.drinkup.favorite.spi.ObjectType;
import java.time.ZonedDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户已调制DTO
 */
@Getter
@Setter
public class UserMadeDTO {
    private Long id;
    private ObjectType objectType;
    private Long objectId;
    private ZonedDateTime madeTime;
    private Object objectDetail;
}
