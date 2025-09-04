package cool.drinkup.drinkup.favorite.internal.entity;

import cool.drinkup.drinkup.favorite.spi.ObjectType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户收藏实体
 */
@Entity
@Table(
        name = "user_favorite",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "object_type", "object_id"})})
@Getter
@Setter
public class UserFavorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "object_type", nullable = false)
    private ObjectType objectType;

    @Column(name = "object_id", nullable = false)
    private Long objectId;

    @Column(name = "favorite_time", nullable = false, updatable = false, columnDefinition = "DATETIME")
    private ZonedDateTime favoriteTime = ZonedDateTime.now(ZoneOffset.UTC);

    @Column(name = "note")
    private String note;

    // 瞬态字段，用于存储关联对象
    @Transient
    private Object favoriteObject;

    // 辅助方法
    public <T> T getFavoriteObject(Class<T> clazz) {
        return clazz.cast(favoriteObject);
    }
}
