package cool.drinkup.drinkup.workflow.internal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import cool.drinkup.drinkup.workflow.internal.model.Material;

public interface MaterialRepository extends JpaRepository<Material, Long> {
    
    /**
     * 根据分类ID获取材料列表
     */
    List<Material> findByCategoryIdAndIsActiveTrueOrderBySortOrderAsc(Long categoryId);

    /**
     * 获取所有活跃的材料，按分类和排序字段排序
     */
    @Query("SELECT m FROM Material m WHERE m.isActive = true ORDER BY m.categoryId ASC, m.sortOrder ASC")
    List<Material> findAllActiveMaterialsOrderByCategoryAndSort();

    /**
     * 根据材料名称搜索（支持中英文）
     */
    @Query("SELECT m FROM Material m WHERE m.isActive = true AND (m.name LIKE %:keyword% OR m.nameEn LIKE %:keyword%) ORDER BY m.sortOrder ASC")
    List<Material> searchByName(@Param("keyword") String keyword);

    /**
     * 根据分类ID列表获取材料
     */
    @Query("SELECT m FROM Material m WHERE m.categoryId IN :categoryIds AND m.isActive = true ORDER BY m.categoryId ASC, m.sortOrder ASC")
    List<Material> findByCategoryIds(@Param("categoryIds") List<Long> categoryIds);
} 