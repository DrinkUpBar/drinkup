package cool.drinkup.drinkup.wine.internal.controller.resp;

import lombok.Data;

@Data
public class WineSceneVo {
    private Long id;
    private String name;
    private String nameEn;
    private String description;
    private String icon;
    private Integer sortOrder;
}
