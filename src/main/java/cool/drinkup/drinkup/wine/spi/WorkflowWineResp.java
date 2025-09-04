package cool.drinkup.drinkup.wine.spi;

import cool.drinkup.drinkup.wine.internal.controller.resp.WorkflowWineVo;
import java.util.List;
import lombok.Data;

@Data
public class WorkflowWineResp {
    private List<WorkflowWineVo> wines;
}
