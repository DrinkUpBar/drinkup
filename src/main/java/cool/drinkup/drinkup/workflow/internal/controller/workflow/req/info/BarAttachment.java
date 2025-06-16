package cool.drinkup.drinkup.workflow.internal.controller.workflow.req.info;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BarAttachment {
    private Long barId;

    private List<Long> selectedStockIdList;
}
