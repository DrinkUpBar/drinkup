package cool.drinkup.drinkup.wine.internal.spi.impl;

import cool.drinkup.drinkup.favorite.spi.FavoriteObjectLoader;
import cool.drinkup.drinkup.favorite.spi.ObjectType;
import cool.drinkup.drinkup.shared.dto.UserWine;
import cool.drinkup.drinkup.wine.internal.mapper.UserWineMapper;
import cool.drinkup.drinkup.wine.internal.repository.UserWineRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserWineFavoriteLoader implements FavoriteObjectLoader<UserWine> {

    private final UserWineRepository userWineRepository;
    private final UserWineMapper userWineMapper;

    @Override
    public Map<Long, UserWine> loadObjects(List<Long> objectIds) {
        return userWineRepository.findAllById(objectIds).stream()
                .map(userWineMapper::toWorkflowUserWineVo)
                .collect(Collectors.toMap(userWine -> Long.parseLong(userWine.getId()), userWine -> userWine));
    }

    @Override
    public boolean validateObject(Long objectId) {
        return userWineRepository.existsById(objectId);
    }

    @Override
    public void afterFavorite(Long objectId, boolean isFavorite) {
        // 更新收藏计数
        userWineRepository.findById(objectId).ifPresent(wine -> {
            wine.setFavoriteCount(wine.getFavoriteCount() + (isFavorite ? 1 : -1));
            userWineRepository.save(wine);
        });
    }

    @Override
    public ObjectType getFavoriteType() {
        return ObjectType.USER_WINE;
    }
}
