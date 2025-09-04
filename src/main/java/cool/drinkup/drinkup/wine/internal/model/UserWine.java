package cool.drinkup.drinkup.wine.internal.model;

import cool.drinkup.drinkup.favorite.spi.ObjectType;
import cool.drinkup.drinkup.shared.enums.CardStyleEnum;
import cool.drinkup.drinkup.shared.enums.ThemeEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "user_wine")
@Getter
@Setter
public class UserWine implements cool.drinkup.drinkup.shared.dto.UserWine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    private String name;
    private String nameEn;
    private String description;
    private String baseIngredient;

    @Column(columnDefinition = "json")
    @JdbcTypeCode(SqlTypes.JSON)
    private String ingredients;

    private String ingredientOverview;
    private String ingredientAlcoholOverview;
    private String method;
    private String garnish;
    private String glassware;
    private String flavor;
    private String mixology;
    private String preparationTime;
    private String alcoholContent;
    private String calories;
    private String invented;
    private String tagMainBaseSpirit;

    @Column(columnDefinition = "json")
    @JdbcTypeCode(SqlTypes.JSON)
    private String tagBaseSpirit;

    private String tagGlass;
    private String tagMixology;
    private String tagCocktailType;

    @Column(columnDefinition = "json")
    @JdbcTypeCode(SqlTypes.JSON)
    private String tagFlavor;

    private String tagComplexity;
    private String tagAbv;

    @Column(columnDefinition = "json")
    @JdbcTypeCode(SqlTypes.JSON)
    private String tagsOthers;

    private String image;
    private String cardImage;
    private String processedImage;
    private String themeStory;
    private String themeFeatureTag;

    @Enumerated(EnumType.STRING)
    private ThemeEnum theme;

    @Enumerated(EnumType.STRING)
    private CardStyleEnum cardStyle;

    @CreationTimestamp
    @Column(name = "create_date", updatable = false, columnDefinition = "DATETIME")
    private ZonedDateTime createDate = ZonedDateTime.now(ZoneOffset.UTC);

    @UpdateTimestamp
    @Column(name = "update_date", columnDefinition = "DATETIME")
    private ZonedDateTime updateDate = ZonedDateTime.now(ZoneOffset.UTC);

    @Column(name = "favorite_count", columnDefinition = "INT DEFAULT 0")
    private Integer favoriteCount = 0;

    @Transient
    private ObjectType favoriteType = ObjectType.USER_WINE;

    @Override
    public String getId() {
        return id.toString();
    }
}
