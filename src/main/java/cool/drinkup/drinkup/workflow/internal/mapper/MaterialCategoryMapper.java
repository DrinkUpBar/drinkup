package cool.drinkup.drinkup.workflow.internal.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import cool.drinkup.drinkup.workflow.internal.controller.material.resp.MaterialCategoryVo;
import cool.drinkup.drinkup.workflow.internal.model.MaterialCategory;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface MaterialCategoryMapper {

    @Mapping(target = "children", ignore = true)
    MaterialCategoryVo toMaterialCategoryVo(MaterialCategory materialCategory);
} 