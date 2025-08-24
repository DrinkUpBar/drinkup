package cool.drinkup.drinkup.favorite.spi;

import cool.drinkup.drinkup.shared.dto.UserWine;
import cool.drinkup.drinkup.shared.dto.Wine;

/**
 * 对象类型枚举 - 用于收藏、已调制等功能
 */
public enum ObjectType {
    WINE("wine", Wine.class),
    USER_WINE("user_wine", UserWine.class);

    private final String value;
    private final Class<?> entityClass;

    ObjectType(String value, Class<?> entityClass) {
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
