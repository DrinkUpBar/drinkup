package cool.drinkup.drinkup.favorite.spi;

import cool.drinkup.drinkup.shared.dto.UserWine;
import cool.drinkup.drinkup.shared.dto.Wine;

/**
 * 收藏类型枚举
 */
public enum FavoriteType {
    WINE("wine", Wine.class),
    USER_WINE("user_wine", UserWine.class);

    private final String value;
    private final Class<?> entityClass;

    FavoriteType(String value, Class<?> entityClass) {
        this.value = value;
        this.entityClass = entityClass;
    }

    public String getValue() {
        return value;
    }

    public Class<?> getEntityClass() {
        return entityClass;
    }
}
