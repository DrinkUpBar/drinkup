package cool.drinkup.drinkup.wine.internal.repository;

import cool.drinkup.drinkup.wine.internal.model.UserWine;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserWineRepository extends JpaRepository<UserWine, Long> {

    Page<UserWine> findByUserId(Long userId, Pageable pageable);

    @Query(value = "SELECT * FROM user_wine WHERE user_id = :userId ORDER BY RAND() LIMIT 1", nativeQuery = true)
    UserWine findRandomUserWine(@Param("userId") Long userId);

    @Query(value = "SELECT * FROM user_wine WHERE user_id = :userId ORDER BY RAND() LIMIT :count", nativeQuery = true)
    List<UserWine> findRandomUserWines(@Param("userId") Long userId, @Param("count") int count);
}
