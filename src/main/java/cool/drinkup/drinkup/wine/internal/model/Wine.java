package cool.drinkup.drinkup.wine.internal.model;

import cool.drinkup.drinkup.favorite.spi.FavoriteType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "wine")
@Getter
@Setter
public class Wine implements cool.drinkup.drinkup.shared.dto.Wine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
    private String tagIba;

    @Column(columnDefinition = "json")
    @JdbcTypeCode(SqlTypes.JSON)
    private String tagsOthers;

    private String image;
    private String cardImage;
    private String processedImage;

    @Column(name = "favorite_count", columnDefinition = "INT DEFAULT 0")
    private Integer favoriteCount = 0;

    @Transient
    private FavoriteType favoriteType = FavoriteType.WINE;

    @Override
    public String getId() {
        return id.toString();
    }
}
