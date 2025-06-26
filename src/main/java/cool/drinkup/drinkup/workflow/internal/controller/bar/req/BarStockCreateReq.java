package cool.drinkup.drinkup.workflow.internal.controller.bar.req;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.List;
import lombok.Data;

@Data
public class BarStockCreateReq {
    @JsonAlias({"bar_stocks", "user_stock"})
    private List<InnerBarStockCreateReq> barStocks;

    @Data
    public static class InnerBarStockCreateReq {
        private String name;

        @JsonAlias({"name_en"})
        private String nameEn;

        private String type;

        @JsonAlias({"icon_type"})
        private String iconType;

        private String description;
    }
}
