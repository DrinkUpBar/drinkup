package cool.drinkup.drinkup.workflow.internal.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

import cool.drinkup.drinkup.workflow.internal.controller.material.resp.MaterialCategoryVo;
import cool.drinkup.drinkup.workflow.internal.model.MaterialCategory;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface MaterialCategoryMapper {

    MaterialCategoryVo toMaterialCategoryVo(MaterialCategory materialCategory);
} 