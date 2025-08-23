package cool.drinkup.drinkup.wine.internal.repository;

import cool.drinkup.drinkup.wine.internal.model.WineCategoryMapping;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WineCategoryMappingRepository extends JpaRepository<WineCategoryMapping, Long> {

    /**
     * 根据分类ID查询酒ID列表（按权重排序）
     */
    @Query("SELECT m.wineId FROM WineCategoryMapping m WHERE m.categoryId = :categoryId ORDER BY" + " m.weight DESC")
    Page<Long> findWineIdsByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);

    /**
     * 根据多个分类ID查询酒ID列表（按权重综合排序）
     */
    @Query("SELECT m.wineId FROM WineCategoryMapping m WHERE m.categoryId IN :categoryIds "
            + "GROUP BY m.wineId ORDER BY SUM(m.weight) DESC")
    Page<Long> findWineIdsByCategories(@Param("categoryIds") List<Long> categoryIds, Pageable pageable);

    /**
     * 根据酒ID查询所属分类
     */
    List<WineCategoryMapping> findByWineId(Long wineId);

    /**
     * 检查酒是否属于指定分类
     */
    boolean existsByWineIdAndCategoryId(Long wineId, Long categoryId);
}
