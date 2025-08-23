package cool.drinkup.drinkup.wine.internal.controller.resp;

import java.util.List;
import lombok.Data;

@Data
public class WineCategoryTreeVo {
    private Long id;
    private String name;
    private String nameEn;
    private String description;
    private String icon;
    private Integer level;
    private Integer sortOrder;
    private List<WineCategoryTreeVo> children;
}
