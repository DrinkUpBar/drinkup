package cool.drinkup.drinkup.workflow.internal.service.bartender.dto;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BartenderParams {
    private String userStock;
    private String userDemand;
    private String theme;
    private String themeEnum;
    private String imagePrompt;

    public Map<String, String> toSubstituterMap() {
        Map<String, String> result = new HashMap<>();
        Field[] fields = this.getClass().getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(this);
                result.put(field.getName(), value != null ? value.toString() : "");
            } catch (IllegalAccessException e) {
                // Skip fields that can't be accessed
                continue;
            }
        }

        return result;
    }
}
