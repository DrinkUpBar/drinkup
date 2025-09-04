package cool.drinkup.drinkup.wine.internal.controller.resp;

import lombok.Data;

@Data
public class WineCategoryVo {
    private Long id;
    private Long sceneId;
    private String name;
    private String nameEn;
    private String description;
    private String icon;
    private Long parentId;
    private Integer level; // 1-一级分类, 2-二级分类
    private Integer sortOrder;
}
