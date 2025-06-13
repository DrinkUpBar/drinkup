package cool.drinkup.drinkup.workflow.internal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.InjectionStrategy;

import cool.drinkup.drinkup.workflow.internal.controller.resp.MaterialVo;
import cool.drinkup.drinkup.workflow.internal.model.Material;

@Mapper(componentModel = "spring", uses = MaterialCategoryMapper.class, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface MaterialMapper {

    MaterialVo toMaterialVo(Material material);
} 