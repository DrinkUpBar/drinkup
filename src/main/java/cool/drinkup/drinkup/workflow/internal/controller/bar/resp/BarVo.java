package cool.drinkup.drinkup.workflow.internal.controller.bar.resp;

import java.util.List;
import lombok.Data;

@Data
public class BarVo {
    private Long id;
    private String name;
    private String description;
    private String image;
    private Integer barImageType;
    private List<BarStockVo> barStocks;
    private List<BarProcurementVo> barProcurements;
}
