package cool.drinkup.drinkup.workflow.internal.mapper;

import cool.drinkup.drinkup.workflow.internal.controller.material.resp.MaterialCategoryVo;
import cool.drinkup.drinkup.workflow.internal.model.MaterialCategory;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface MaterialCategoryMapper {

    @Mapping(target = "children", ignore = true)
    MaterialCategoryVo toMaterialCategoryVo(MaterialCategory materialCategory);
}
