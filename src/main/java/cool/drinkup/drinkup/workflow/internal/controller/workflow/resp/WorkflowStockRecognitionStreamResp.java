package cool.drinkup.drinkup.workflow.internal.controller.workflow.resp;

import cool.drinkup.drinkup.workflow.internal.model.BarStock;
import java.util.List;
import lombok.Data;

@Data
public class WorkflowStockRecognitionStreamResp {
    private boolean isDone;
    private String text;
    private Long barId;
    private List<BarStock> recognizedStocks;
}
