package cool.drinkup.drinkup.workflow.internal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

import cool.drinkup.drinkup.workflow.internal.constant.WorkflowConstant;
import cool.drinkup.drinkup.workflow.internal.controller.bar.req.BarStockCreateReq;
import cool.drinkup.drinkup.workflow.internal.controller.bar.resp.BarStockVo;
import cool.drinkup.drinkup.workflow.internal.model.Bar;
import cool.drinkup.drinkup.workflow.internal.model.BarStock;

@Mapper(componentModel = "spring")
public interface BatStockMapper {

    @Mapping(target = "tag",  constant = WorkflowConstant.BAR_STOCK_TAG)
    BarStockVo toBarStockVo(BarStock barStock);
    
    default List<BarStock> toBarStock(BarStockCreateReq barStockCreateReq, Long barId) {
        if (barStockCreateReq == null) {
            return null;
        }
        
        List<BarStock> barStocks = new java.util.ArrayList<>();
        
        barStockCreateReq.getBarStocks().forEach(item -> {
            BarStock barStock = new BarStock();
            Bar bar = new Bar();
            bar.setId(barId);
            barStock.setBar(bar);
            barStock.setName(item.getName());
            barStock.setType(item.getType());
            barStock.setIconType(item.getIconType());
            barStock.setDescription(item.getDescription());
            barStocks.add(barStock);
        });
        
        return barStocks;
    }
}
