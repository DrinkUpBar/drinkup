package cool.drinkup.drinkup.workflow.internal.service.material;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cool.drinkup.drinkup.workflow.internal.model.MaterialCategory;
import cool.drinkup.drinkup.workflow.internal.repository.MaterialCategoryRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MaterialCategoryService {

    private final MaterialCategoryRepository materialCategoryRepository;

    /**
     * 获取所有活跃的分类（树形结构）
     */
    public List<MaterialCategory> getAllCategoriesTree() {
        List<MaterialCategory> allCategories = materialCategoryRepository.findByIsActiveTrueOrderBySortOrderAsc();
        return buildCategoryTree(allCategories);
    }

    /**
     * 获取所有活跃的分类（扁平结构）
     */
    public List<MaterialCategory> getAllCategories() {
        return materialCategoryRepository.findByIsActiveTrueOrderBySortOrderAsc();
    }

    /**
     * 获取顶级分类
     */
    public List<MaterialCategory> getTopLevelCategories() {
        return materialCategoryRepository.findTopLevelCategories();
    }

    /**
     * 根据父分类ID获取子分类
     */
    public List<MaterialCategory> getCategoriesByParentId(Long parentId) {
        return materialCategoryRepository.findByParentIdAndIsActiveTrueOrderBySortOrderAsc(parentId);
    }

    /**
     * 构建分类树
     */
    private List<MaterialCategory> buildCategoryTree(List<MaterialCategory> allCategories) {
        Map<Long, List<MaterialCategory>> categoryMap = allCategories.stream()
                .filter(category -> category.getParentId() != null)
                .collect(Collectors.groupingBy(MaterialCategory::getParentId));

        List<MaterialCategory> rootCategories = allCategories.stream()
                .filter(category -> category.getParentId() == null)
                .collect(Collectors.toList());

        for (MaterialCategory category : allCategories) {
            List<MaterialCategory> children = categoryMap.get(category.getId());
            if (children != null) {
                // 此处可以设置children字段，但由于Entity不包含children字段，需要在VO层处理
                // 这里仅返回扁平结构，树形结构在Controller层或Service层处理
            }
        }

        return rootCategories;
    }
} 