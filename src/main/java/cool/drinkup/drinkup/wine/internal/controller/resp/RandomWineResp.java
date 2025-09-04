package cool.drinkup.drinkup.wine.internal.controller.resp;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RandomWineResp {
    private List<RandomWineContent> wines; // 多个酒

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RandomWineContent {
        private String type;
        private Object wine;
    }
}
