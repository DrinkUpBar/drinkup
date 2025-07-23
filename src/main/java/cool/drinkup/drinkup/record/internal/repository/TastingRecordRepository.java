package cool.drinkup.drinkup.record.internal.repository;

import cool.drinkup.drinkup.record.internal.model.TastingRecord;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TastingRecordRepository extends JpaRepository<TastingRecord, Long> {

    /**
     * 根据饮品ID和饮品类型查询品鉴记录
     */
    List<TastingRecord> findByBeverageIdAndBeverageTypeAndStatus(Long beverageId, String beverageType, Integer status);

    /**
     * 根据用户ID查询品鉴记录
     */
    List<TastingRecord> findByUserIdAndStatusOrderByCreatedTimeDesc(Long userId, Integer status);

    /**
     * 根据记录ID、用户ID和状态查询品鉴记录
     */
    Optional<TastingRecord> findByIdAndUserIdAndStatus(Long id, Long userId, Integer status);

    /**
     * 根据饮品ID和饮品类型查询品鉴记录，按创建时间降序排序
     */
    List<TastingRecord> findByBeverageIdAndBeverageTypeAndUserIdAndStatusOrderByCreatedTimeDesc(
            Long beverageId, String beverageType, Long userId, Integer status);
}
