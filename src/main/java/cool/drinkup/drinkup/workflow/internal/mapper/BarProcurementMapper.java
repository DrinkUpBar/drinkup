package cool.drinkup.drinkup.workflow.internal.mapper;

import cool.drinkup.drinkup.workflow.internal.constant.WorkflowConstant;
import cool.drinkup.drinkup.workflow.internal.controller.bar.resp.BarProcurementVo;
import cool.drinkup.drinkup.workflow.internal.model.BarProcurement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BarProcurementMapper {

    @Mapping(target = "tag", constant = WorkflowConstant.BAR_PROCUREMENT_TAG)
    BarProcurementVo toBarProcurementVo(BarProcurement barProcurement);
}
