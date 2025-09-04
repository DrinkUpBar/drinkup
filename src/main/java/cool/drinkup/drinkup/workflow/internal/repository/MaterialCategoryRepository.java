package cool.drinkup.drinkup.workflow.internal.repository;

import cool.drinkup.drinkup.workflow.internal.model.MaterialCategory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MaterialCategoryRepository extends JpaRepository<MaterialCategory, Long> {

    /**
     * 获取所有活跃的分类，按排序字段排序
     */
    List<MaterialCategory> findByIsActiveTrueOrderBySortOrderAsc();

    /**
     * 根据父分类ID获取子分类
     */
    List<MaterialCategory> findByParentIdAndIsActiveTrueOrderBySortOrderAsc(Long parentId);

    /**
     * 获取所有顶级分类（一级分类）
     */
    @Query("SELECT mc FROM MaterialCategory mc WHERE mc.parentId IS NULL AND mc.isActive = true"
            + " ORDER BY mc.sortOrder ASC")
    List<MaterialCategory> findTopLevelCategories();
}
