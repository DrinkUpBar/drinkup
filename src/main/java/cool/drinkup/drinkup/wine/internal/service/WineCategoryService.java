package cool.drinkup.drinkup.wine.internal.service;

import cool.drinkup.drinkup.wine.internal.controller.resp.WineCategoryTreeVo;
import cool.drinkup.drinkup.wine.internal.controller.resp.WineSceneCategoryTreeVo;
import cool.drinkup.drinkup.wine.internal.mapper.WineCategoryMapper;
import cool.drinkup.drinkup.wine.internal.model.Wine;
import cool.drinkup.drinkup.wine.internal.model.WineCategory;
import cool.drinkup.drinkup.wine.internal.model.WineScene;
import cool.drinkup.drinkup.wine.internal.repository.WineCategoryMappingRepository;
import cool.drinkup.drinkup.wine.internal.repository.WineCategoryRepository;
import cool.drinkup.drinkup.wine.internal.repository.WineRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WineCategoryService {

    private final WineCategoryRepository categoryRepository;
    private final WineCategoryMappingRepository mappingRepository;
    private final WineRepository wineRepository;
    private final WineSceneService sceneService;
    private final WineCategoryMapper categoryMapper;

    /**
     * 获取某个场景下的一级分类
     */
    @Cacheable(
            value = "wine:categories",
            key = "'scene:' + #sceneId + ':level1'",
            cacheManager = "categoryCacheManager")
    public List<WineCategory> getLevel1CategoriesByScene(Long sceneId) {
        return categoryRepository.findBySceneIdAndLevelAndIsActiveTrueOrderBySortOrderAsc(sceneId, 1);
    }

    /**
     * 获取某个一级分类下的二级分类
     */
    @Cacheable(
            value = "wine:categories",
            key = "'category:' + #categoryId + ':children'",
            cacheManager = "categoryCacheManager")
    public List<WineCategory> getLevel2CategoriesByParent(Long categoryId) {
        return categoryRepository.findByParentIdAndIsActiveTrueOrderBySortOrderAsc(categoryId);
    }

    /**
     * 获取某个场景下的完整分类树
     */
    @Cacheable(value = "wine:categories", key = "'scene:' + #sceneId + ':tree'", cacheManager = "categoryCacheManager")
    public WineSceneCategoryTreeVo getSceneCategoryTree(Long sceneId) {
        WineScene scene = sceneService.getSceneById(sceneId);

        // 构建场景节点
        WineSceneCategoryTreeVo sceneTree = new WineSceneCategoryTreeVo();
        sceneTree.setScene(categoryMapper.toSceneVo(scene));

        // 获取一级分类
        List<WineCategory> level1Categories = getLevel1CategoriesByScene(sceneId);
        List<WineCategoryTreeVo> categoryTrees =
                level1Categories.stream().map(this::buildCategoryTree).collect(Collectors.toList());

        sceneTree.setCategories(categoryTrees);
        return sceneTree;
    }

    /**
     * 获取所有场景的完整分类树
     */
    @Cacheable(value = "wine:categories", key = "'all:tree'", cacheManager = "categoryCacheManager")
    public List<WineSceneCategoryTreeVo> getAllSceneCategoryTrees() {
        List<WineScene> scenes = sceneService.getAllScenes();
        return scenes.stream().map(scene -> getSceneCategoryTree(scene.getId())).collect(Collectors.toList());
    }

    /**
     * 根据场景查询酒列表
     */
    public Page<Wine> getWinesByScene(Long sceneId, Pageable pageable) {
        List<Long> categoryIds = categoryRepository.findCategoryIdsBySceneId(sceneId);
        if (categoryIds.isEmpty()) {
            return Page.empty(pageable);
        }

        Page<Long> wineIds = mappingRepository.findWineIdsByCategories(categoryIds, pageable);
        List<Wine> wines = wineRepository.findAllById(wineIds.getContent());

        return new PageImpl<>(wines, pageable, wineIds.getTotalElements());
    }

    /**
     * 根据分类查询酒列表（支持一级和二级分类）
     */
    public Page<Wine> getWinesByCategory(Long categoryId, Pageable pageable) {
        // 获取该分类及其所有子分类的ID
        List<Long> allCategoryIds = categoryRepository.findCategoryAndSubCategoryIds(categoryId);

        Page<Long> wineIds = mappingRepository.findWineIdsByCategories(allCategoryIds, pageable);
        List<Wine> wines = wineRepository.findAllById(wineIds.getContent());

        return new PageImpl<>(wines, pageable, wineIds.getTotalElements());
    }

    private WineCategoryTreeVo buildCategoryTree(WineCategory category) {
        WineCategoryTreeVo treeVo = categoryMapper.toCategoryTreeVo(category);

        // 如果是一级分类，获取其二级分类
        if (category.getLevel() == 1) {
            List<WineCategory> level2Categories = getLevel2CategoriesByParent(category.getId());
            List<WineCategoryTreeVo> children =
                    level2Categories.stream().map(this::buildCategoryTree).collect(Collectors.toList());
            treeVo.setChildren(children);
        }

        return treeVo;
    }

    @Cacheable(
            value = "wine:categories",
            key = "'wine:' + #wineId + ':categoryIds'",
            cacheManager = "categoryCacheManager")
    public List<String> getCategoryIdsByWineId(Long wineId) {
        return mappingRepository.findByWineId(wineId).stream()
                .map(mapping -> mapping.getCategoryId().toString())
                .collect(Collectors.toList());
    }
}
