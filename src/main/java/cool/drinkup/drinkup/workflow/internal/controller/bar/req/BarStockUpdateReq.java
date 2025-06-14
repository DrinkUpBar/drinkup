package cool.drinkup.drinkup.workflow.internal.controller.bar.req;

import lombok.Data;

@Data
public class BarStockUpdateReq {
    private String name;
    private String type;
    private String iconType;
    private String description;
}