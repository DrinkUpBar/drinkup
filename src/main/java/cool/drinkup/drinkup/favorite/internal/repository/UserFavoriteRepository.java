package cool.drinkup.drinkup.favorite.internal.repository;

import cool.drinkup.drinkup.favorite.internal.entity.UserFavorite;
import cool.drinkup.drinkup.favorite.spi.ObjectType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserFavoriteRepository
        extends JpaRepository<UserFavorite, Long>, JpaSpecificationExecutor<UserFavorite> {

    // 基础查询方法
    Optional<UserFavorite> findByUserIdAndObjectTypeAndObjectId(Long userId, ObjectType objectType, Long objectId);

    boolean existsByUserIdAndObjectTypeAndObjectId(Long userId, ObjectType objectType, Long objectId);

    @Modifying
    @Query("DELETE FROM UserFavorite f WHERE f.userId = :userId AND f.objectType = :objectType AND"
            + " f.objectId = :objectId")
    void deleteByUserIdAndObjectTypeAndObjectId(
            @Param("userId") Long userId, @Param("objectType") ObjectType objectType, @Param("objectId") Long objectId);

    // 分页查询用户收藏
    Page<UserFavorite> findByUserIdOrderByFavoriteTimeDesc(Long userId, Pageable pageable);

    // 按类型查询
    Page<UserFavorite> findByUserIdAndObjectTypeOrderByFavoriteTimeDesc(
            Long userId, ObjectType objectType, Pageable pageable);

    // 统计查询
    long countByObjectTypeAndObjectId(ObjectType objectType, Long objectId);

    // 批量查询
    @Query("SELECT f FROM UserFavorite f WHERE f.userId = :userId AND f.objectType = :objectType"
            + " AND f.objectId IN :objectIds")
    List<UserFavorite> findByUserIdAndObjectTypeAndObjectIdIn(
            @Param("userId") Long userId,
            @Param("objectType") ObjectType objectType,
            @Param("objectIds") List<Long> objectIds);
}
