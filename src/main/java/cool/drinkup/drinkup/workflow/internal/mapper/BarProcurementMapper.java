package cool.drinkup.drinkup.workflow.internal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import cool.drinkup.drinkup.workflow.internal.constant.WorkflowConstant;
import cool.drinkup.drinkup.workflow.internal.controller.bar.resp.BarProcurementVo;
import cool.drinkup.drinkup.workflow.internal.model.BarProcurement;

@Mapper(componentModel = "spring")
public interface BarProcurementMapper {
    
    @Mapping(target = "tag", constant = WorkflowConstant.BAR_PROCUREMENT_TAG)
    BarProcurementVo toBarProcurementVo(BarProcurement barProcurement);

}
