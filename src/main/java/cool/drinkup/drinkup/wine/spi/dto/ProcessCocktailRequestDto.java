package cool.drinkup.drinkup.wine.spi.dto;

import java.util.List;
import lombok.Data;

@Data
public class ProcessCocktailRequestDto {
    private String userInput;
    private List<Long> categoryIds;
}
