package cool.drinkup.drinkup.record.internal.repository;

import cool.drinkup.drinkup.record.internal.model.TastingRecordImage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TastingRecordImageRepository extends JpaRepository<TastingRecordImage, Long> {

    /**
     * 根据品鉴记录ID查询图片
     */
    List<TastingRecordImage> findByRecordIdOrderByCreatedTimeAsc(Long recordId);

    /**
     * 根据品鉴记录ID查询封面图片
     */
    List<TastingRecordImage> findByRecordIdAndIsCover(Long recordId, Integer isCover);
}
