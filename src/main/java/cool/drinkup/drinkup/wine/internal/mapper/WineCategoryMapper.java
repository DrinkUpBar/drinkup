package cool.drinkup.drinkup.wine.internal.mapper;

import cool.drinkup.drinkup.wine.internal.controller.resp.WineCategoryTreeVo;
import cool.drinkup.drinkup.wine.internal.controller.resp.WineCategoryVo;
import cool.drinkup.drinkup.wine.internal.controller.resp.WineSceneCategoryTreeVo;
import cool.drinkup.drinkup.wine.internal.controller.resp.WineSceneVo;
import cool.drinkup.drinkup.wine.internal.model.WineCategory;
import cool.drinkup.drinkup.wine.internal.model.WineScene;
import java.util.List;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface WineCategoryMapper {

    WineSceneVo toSceneVo(WineScene scene);

    WineCategoryVo toCategoryVo(WineCategory category);

    @Mapping(target = "children", ignore = true)
    WineCategoryTreeVo toCategoryTreeVo(WineCategory category);

    List<WineSceneVo> toSceneVoList(List<WineScene> scenes);

    List<WineCategoryVo> toCategoryVoList(List<WineCategory> categories);

    @Mapping(target = "scene", source = "scene")
    @Mapping(target = "categories", source = "categories")
    WineSceneCategoryTreeVo toSceneCategoryTreeVo(WineSceneVo scene, List<WineCategoryTreeVo> categories);
}
