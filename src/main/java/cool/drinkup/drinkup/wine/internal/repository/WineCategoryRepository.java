package cool.drinkup.drinkup.wine.internal.repository;

import cool.drinkup.drinkup.wine.internal.model.WineCategory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WineCategoryRepository extends JpaRepository<WineCategory, Long> {

    /**
     * 查询某个场景下的所有一级分类
     */
    List<WineCategory> findBySceneIdAndLevelAndIsActiveTrueOrderBySortOrderAsc(Long sceneId, Integer level);

    /**
     * 查询某个一级分类下的所有二级分类
     */
    List<WineCategory> findByParentIdAndIsActiveTrueOrderBySortOrderAsc(Long parentId);

    /**
     * 查询某个场景下的所有分类（包括一级和二级）
     */
    List<WineCategory> findBySceneIdAndIsActiveTrueOrderByLevelAscSortOrderAsc(Long sceneId);

    /**
     * 根据场景和分类名称查询
     */
    Optional<WineCategory> findBySceneIdAndNameAndParentIdAndIsActiveTrue(Long sceneId, String name, Long parentId);

    /**
     * 查询某个场景下的所有分类ID
     */
    @Query("SELECT c.id FROM WineCategory c WHERE c.sceneId = :sceneId AND c.isActive = true")
    List<Long> findCategoryIdsBySceneId(@Param("sceneId") Long sceneId);

    /**
     * 递归查询某个分类及其所有子分类ID
     */
    @Query(
            value = "WITH RECURSIVE category_tree AS (  SELECT id FROM wine_category WHERE id ="
                    + " :categoryId AND is_active = true   UNION ALL   SELECT c.id FROM"
                    + " wine_category c   INNER JOIN category_tree ct ON c.parent_id = ct.id  "
                    + " WHERE c.is_active = true) SELECT id FROM category_tree",
            nativeQuery = true)
    List<Long> findCategoryAndSubCategoryIds(@Param("categoryId") Long categoryId);
}
