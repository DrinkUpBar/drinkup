package cool.drinkup.drinkup.workflow.internal.controller.bar.req;

import lombok.Data;

@Data
public class BarCreateReq {
    private String name;
    private String userId;
    private String image;
    private String description;
}
