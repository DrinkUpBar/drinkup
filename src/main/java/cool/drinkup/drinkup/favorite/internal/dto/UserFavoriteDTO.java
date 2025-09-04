package cool.drinkup.drinkup.favorite.internal.dto;

import cool.drinkup.drinkup.favorite.spi.ObjectType;
import java.time.ZonedDateTime;
import lombok.Data;

@Data
public class UserFavoriteDTO {
    private Long id;
    private ObjectType objectType;
    private Long objectId;
    private ZonedDateTime favoriteTime;
    private String note;
    private Object objectDetail;
}
