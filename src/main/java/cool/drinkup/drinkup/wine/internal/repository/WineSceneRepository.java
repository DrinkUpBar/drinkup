package cool.drinkup.drinkup.wine.internal.repository;

import cool.drinkup.drinkup.wine.internal.model.WineScene;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WineSceneRepository extends JpaRepository<WineScene, Long> {

    /**
     * 查询所有活跃场景
     */
    List<WineScene> findByIsActiveTrueOrderBySortOrderAsc();

    /**
     * 根据名称查询场景
     */
    Optional<WineScene> findByNameAndIsActiveTrue(String name);
}
