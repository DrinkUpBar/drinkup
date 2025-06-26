package cool.drinkup.drinkup.wine.internal.repository;

import cool.drinkup.drinkup.wine.internal.model.Wine;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WineRepository extends JpaRepository<Wine, Long> {
    Page<Wine> findByTagMainBaseSpirit(String tagMainBaseSpirit, Pageable pageable);

    @Query("SELECT w FROM Wine w WHERE (:tagMainBaseSpirit IS NULL OR w.tagMainBaseSpirit ="
            + " :tagMainBaseSpirit) AND (:tagIba IS NULL OR w.tagIba = :tagIba)")
    Page<Wine> findByTagMainBaseSpiritAndTagIbaWithNullHandling(
            @Param("tagMainBaseSpirit") String tagMainBaseSpirit, @Param("tagIba") String tagIba, Pageable pageable);

    Page<Wine> findAll(Pageable pageable);

    @Query(value = "SELECT * FROM wine ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Wine findRandomWine();

    @Query(value = "SELECT * FROM wine ORDER BY RAND() LIMIT :count", nativeQuery = true)
    List<Wine> findRandomWines(@Param("count") int count);
}
