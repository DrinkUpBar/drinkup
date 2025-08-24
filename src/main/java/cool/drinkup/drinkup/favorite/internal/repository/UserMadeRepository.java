package cool.drinkup.drinkup.favorite.internal.repository;

import cool.drinkup.drinkup.favorite.internal.entity.UserMade;
import cool.drinkup.drinkup.favorite.spi.ObjectType;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserMadeRepository extends JpaRepository<UserMade, Long> {

    boolean existsByUserIdAndObjectTypeAndObjectId(Long userId, ObjectType objectType, Long objectId);

    void deleteByUserIdAndObjectTypeAndObjectId(Long userId, ObjectType objectType, Long objectId);

    Page<UserMade> findByUserIdOrderByMadeTimeDesc(Long userId, Pageable pageable);

    Page<UserMade> findByUserIdAndObjectTypeOrderByMadeTimeDesc(Long userId, ObjectType objectType, Pageable pageable);

    List<UserMade> findByUserIdAndObjectTypeAndObjectIdIn(Long userId, ObjectType objectType, List<Long> objectIds);
}
