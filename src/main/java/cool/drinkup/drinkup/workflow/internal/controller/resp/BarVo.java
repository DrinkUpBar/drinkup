package cool.drinkup.drinkup.workflow.internal.controller.resp;

import java.util.List;

import cool.drinkup.drinkup.workflow.internal.model.BarProcurement;
import cool.drinkup.drinkup.workflow.internal.model.BarStock;
import lombok.Data;

@Data
public class BarVo {
    private Long id;
    private String name;
    private String description;
    private String image;
    private Integer barImageType;
    private List<BarStock> barStocks;
    private List<BarProcurement> barProcurements;
}
