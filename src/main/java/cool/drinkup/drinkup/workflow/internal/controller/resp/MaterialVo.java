package cool.drinkup.drinkup.workflow.internal.controller.resp;

import lombok.Data;

@Data
public class MaterialVo {
    private Long id;
    private String name;
    private String nameEn;
    private Long categoryId;
    private String description;
    private Boolean isActive;
    private Integer sortOrder;
    private String createdDate;
    private String updatedDate;
    private MaterialCategoryVo category;
} 