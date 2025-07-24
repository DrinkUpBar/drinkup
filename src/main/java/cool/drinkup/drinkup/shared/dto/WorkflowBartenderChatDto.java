package cool.drinkup.drinkup.shared.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import cool.drinkup.drinkup.shared.enums.CardStyleEnum;
import cool.drinkup.drinkup.shared.enums.ThemeEnum;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkflowBartenderChatDto {
    private String id;
    private String name;

    @JsonAlias("name_en")
    private String nameEn;

    private String description;

    @JsonAlias("base_ingredient")
    private String baseIngredient;

    private List<Ingredient> ingredients;

    @JsonAlias("ingredient_overview")
    private String ingredientOverview;

    @JsonAlias("ingredient_alcohol_overview")
    private String ingredientAlcoholOverview;

    private String method;
    private String garnish;
    private String glassware;
    private String flavor;
    private String mixology;

    @JsonAlias("preparation_time")
    private String preparationTime;

    @JsonAlias("alcohol_content")
    private String alcoholContent;

    private String calories;
    private String invented;

    @JsonAlias("tag_main_base_spirit")
    private String tagMainBaseSpirit;

    @JsonAlias("tag_base_spirits")
    @JsonDeserialize(using = StringOrListDeserializer.class)
    private List<String> tagBaseSpirit;

    @JsonAlias("tag_glass")
    private String tagGlass;

    @JsonAlias("tag_mixology")
    private String tagMixology;

    @JsonAlias("tag_cocktail_type")
    private String tagCocktailType;

    @JsonAlias("tag_flavor")
    private List<String> tagFlavor;

    @JsonAlias("tag_complexity")
    private String tagComplexity;

    @JsonAlias("tag_abv")
    private String tagAbv;

    @JsonAlias("tag_iba")
    private String tagIba;

    @JsonAlias("tags_others")
    private List<String> tagsOthers;

    @JsonAlias("theme_feature_tag")
    private String themeFeatureTag;

    @JsonAlias("theme_story")
    private String themeStory;

    private ThemeEnum theme;

    private CardStyleEnum cardStyle;

    private String image;

    @JsonAlias("card_image")
    private String cardImage;

    @JsonAlias("processed_image")
    private String processedImage;

    @JsonAlias("image_prompt")
    private String imagePrompt;

    private String favoriteType = "USER_WINE";

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Ingredient {
        @JsonAlias("ingredient_name")
        private String ingredientName;

        @JsonAlias("ingredient_name_en")
        private String ingredientNameEn;

        @JsonAlias({"ingredient_icon_type", "ingredient_type"})
        private String ingredientIconType;

        @JsonAlias("ml")
        private String ml;

        @JsonAlias("oz")
        private String oz;

        @JsonAlias("cl")
        private String cl;
    }

    public static class StringOrListDeserializer extends JsonDeserializer<List<String>> {
        @Override
        public List<String> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.getCodec().readTree(p);
            List<String> result = new ArrayList<>();

            if (node.isTextual()) {
                result.add(node.asText());
            } else if (node.isArray()) {
                node.forEach(element -> result.add(element.asText()));
            }

            return result;
        }
    }
}
