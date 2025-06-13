package cool.drinkup.drinkup.workflow.internal.controller.resp;


import java.util.List;

import lombok.Data;

@Data
public class MaterialCategoryVo {
    private Long id;
    private String name;
    private Long parentId;
    private Integer sortOrder;
    private String icon;
    private Boolean isActive;
    private String createdDate;
    private String updatedDate;
    private List<MaterialCategoryVo> children;
} 