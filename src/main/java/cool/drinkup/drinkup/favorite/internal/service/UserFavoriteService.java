package cool.drinkup.drinkup.favorite.internal.service;

import cool.drinkup.drinkup.favorite.internal.controller.req.CheckFavoriteItemRequest;
import cool.drinkup.drinkup.favorite.internal.dto.UserFavoriteDTO;
import cool.drinkup.drinkup.favorite.internal.entity.UserFavorite;
import cool.drinkup.drinkup.favorite.internal.repository.UserFavoriteRepository;
import cool.drinkup.drinkup.favorite.spi.FavoriteObjectLoader;
import cool.drinkup.drinkup.favorite.spi.ObjectType;
import cool.drinkup.drinkup.shared.dto.UserWine;
import cool.drinkup.drinkup.shared.dto.Wine;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class UserFavoriteService {

    private final UserFavoriteRepository favoriteRepository;
    private final Map<ObjectType, FavoriteObjectLoader<?>> loaderMap;

    public UserFavoriteService(UserFavoriteRepository favoriteRepository, List<FavoriteObjectLoader<?>> loaders) {
        this.favoriteRepository = favoriteRepository;
        this.loaderMap = new HashMap<>();
        for (FavoriteObjectLoader<?> loader : loaders) {
            this.loaderMap.put(loader.getFavoriteType(), loader);
        }
    }

    // 添加收藏
    @Transactional
    public UserFavorite addFavorite(Long userId, ObjectType favoriteType, Long objectId, String note) {
        // 1. 验证对象是否存在
        FavoriteObjectLoader<?> loader = loaderMap.get(favoriteType);
        if (loader == null || !loader.validateObject(objectId)) {
            throw new RuntimeException("收藏对象不存在");
        }

        // 2. 检查是否已收藏
        if (favoriteRepository.existsByUserIdAndObjectTypeAndObjectId(userId, favoriteType, objectId)) {
            throw new RuntimeException("已经收藏过了");
        }

        // 3. 创建收藏记录
        UserFavorite favorite = new UserFavorite();
        favorite.setUserId(userId);
        favorite.setObjectType(favoriteType);
        favorite.setObjectId(objectId);
        favorite.setNote(note);
        favorite.setFavoriteTime(ZonedDateTime.now(ZoneOffset.UTC));

        UserFavorite saved = favoriteRepository.save(favorite);

        // 4. 触发后续操作
        loader.afterFavorite(objectId, true);

        return saved;
    }

    // 取消收藏
    @Transactional
    public void removeFavorite(Long userId, ObjectType objectType, Long objectId) {
        favoriteRepository.deleteByUserIdAndObjectTypeAndObjectId(userId, objectType, objectId);

        FavoriteObjectLoader<?> loader = loaderMap.get(objectType);
        if (loader != null) {
            loader.afterFavorite(objectId, false);
        }
    }

    // 获取用户收藏列表（带详情）
    public Page<UserFavoriteDTO> getUserFavoritesWithDetails(Long userId, ObjectType objectType, Pageable pageable) {
        // 1. 查询收藏记录
        Page<UserFavorite> favoritePage = (objectType == null)
                ? favoriteRepository.findByUserIdOrderByFavoriteTimeDesc(userId, pageable)
                : favoriteRepository.findByUserIdAndObjectTypeOrderByFavoriteTimeDesc(userId, objectType, pageable);

        // 2. 按类型分组
        Map<ObjectType, List<UserFavorite>> groupedFavorites =
                favoritePage.getContent().stream().collect(Collectors.groupingBy(UserFavorite::getObjectType));

        // 3. 批量加载关联对象
        List<UserFavoriteDTO> dtoList = new ArrayList<>();
        for (Map.Entry<ObjectType, List<UserFavorite>> entry : groupedFavorites.entrySet()) {
            ObjectType type = entry.getKey();
            List<UserFavorite> favorites = entry.getValue();
            List<Long> objectIds =
                    favorites.stream().map(UserFavorite::getObjectId).collect(Collectors.toList());

            FavoriteObjectLoader<?> loader = loaderMap.get(type);
            if (loader != null) {
                Map<Long, ?> objectMap = loader.loadObjects(objectIds);

                // 4. 组装DTO
                for (UserFavorite favorite : favorites) {
                    Object obj = objectMap.get(favorite.getObjectId());
                    if (obj != null) {
                        dtoList.add(convertToDTO(favorite, obj));
                    }
                }
            }
        }

        // 5. 按原始顺序排序
        List<Long> originalIds =
                favoritePage.getContent().stream().map(UserFavorite::getId).collect(Collectors.toList());

        dtoList.sort(Comparator.comparingInt(dto -> originalIds.indexOf(dto.getId())));

        return new PageImpl<>(dtoList, pageable, favoritePage.getTotalElements());
    }

    // 检查是否已收藏
    public boolean isFavorited(Long userId, ObjectType objectType, Long objectId) {
        return favoriteRepository.existsByUserIdAndObjectTypeAndObjectId(userId, objectType, objectId);
    }

    // 批量检查收藏状态
    public Map<Long, Boolean> checkFavoriteStatus(Long userId, ObjectType objectType, List<Long> objectIds) {
        List<UserFavorite> favorites =
                favoriteRepository.findByUserIdAndObjectTypeAndObjectIdIn(userId, objectType, objectIds);
        Set<Long> favoritedIds =
                favorites.stream().map(UserFavorite::getObjectId).collect(Collectors.toSet());

        return objectIds.stream().collect(Collectors.toMap(Function.identity(), favoritedIds::contains));
    }

    // 批量检查多种类型的收藏状态
    public Map<ObjectType, Map<Long, Boolean>> checkFavoriteStatusMulti(
            Long userId, List<CheckFavoriteItemRequest> requests) {
        // 1. 按类型分组
        Map<ObjectType, List<Long>> typeToIds = requests.stream()
                .collect(Collectors.groupingBy(
                        CheckFavoriteItemRequest::getObjectType,
                        Collectors.mapping(CheckFavoriteItemRequest::getObjectId, Collectors.toList())));

        // 2. 对每种类型批量查询
        Map<ObjectType, Map<Long, Boolean>> result = new HashMap<>();
        for (Map.Entry<ObjectType, List<Long>> entry : typeToIds.entrySet()) {
            ObjectType type = entry.getKey();
            List<Long> objectIds = entry.getValue();
            result.put(type, checkFavoriteStatus(userId, type, objectIds));
        }

        return result;
    }

    // 转换为DTO
    private UserFavoriteDTO convertToDTO(UserFavorite favorite, Object object) {
        UserFavoriteDTO dto = new UserFavoriteDTO();
        dto.setId(favorite.getId());
        dto.setObjectType(favorite.getObjectType());
        dto.setObjectId(favorite.getObjectId());
        dto.setFavoriteTime(favorite.getFavoriteTime());
        dto.setNote(favorite.getNote());

        // 根据类型设置不同的字段
        if (object instanceof Wine) {
            Wine wine = (Wine) object;
            dto.setObjectDetail(wine);
        } else if (object instanceof UserWine) {
            UserWine userWine = (UserWine) object;
            dto.setObjectDetail(userWine);
        }

        return dto;
    }
}
