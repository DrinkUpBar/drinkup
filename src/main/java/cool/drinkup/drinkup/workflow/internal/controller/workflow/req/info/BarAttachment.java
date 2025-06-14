package cool.drinkup.drinkup.workflow.internal.controller.workflow.req.info;

import java.util.List;

import lombok.Data;

@Data
public class BarAttachment {
    private Long barId;

    private List<Long> selectedStockIdList;
}
