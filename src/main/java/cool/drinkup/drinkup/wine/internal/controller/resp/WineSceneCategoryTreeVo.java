package cool.drinkup.drinkup.wine.internal.controller.resp;

import java.util.List;
import lombok.Data;

@Data
public class WineSceneCategoryTreeVo {
    private WineSceneVo scene;
    private List<WineCategoryTreeVo> categories;
}
