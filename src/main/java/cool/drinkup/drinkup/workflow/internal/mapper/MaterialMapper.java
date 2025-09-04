package cool.drinkup.drinkup.workflow.internal.mapper;

import cool.drinkup.drinkup.workflow.internal.constant.WorkflowConstant;
import cool.drinkup.drinkup.workflow.internal.controller.material.resp.MaterialVo;
import cool.drinkup.drinkup.workflow.internal.model.Material;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        componentModel = "spring",
        uses = MaterialCategoryMapper.class,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface MaterialMapper {

    @Mapping(target = "tag", constant = WorkflowConstant.MATERIAL_TAG)
    MaterialVo toMaterialVo(Material material);
}
