package cool.drinkup.drinkup.wine.internal.spi.impl;

import cool.drinkup.drinkup.favorite.spi.FavoriteObjectLoader;
import cool.drinkup.drinkup.favorite.spi.ObjectType;
import cool.drinkup.drinkup.shared.dto.Wine;
import cool.drinkup.drinkup.wine.internal.mapper.WineMapper;
import cool.drinkup.drinkup.wine.internal.repository.WineRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WineFavoriteLoader implements FavoriteObjectLoader<Wine> {

    private final WineRepository wineRepository;
    private final WineMapper wineMapper;

    @Override
    public Map<Long, Wine> loadObjects(List<Long> objectIds) {
        return wineRepository.findAllById(objectIds).stream()
                .map(wineMapper::toWineVo)
                .collect(Collectors.toMap(wine -> Long.parseLong(wine.getId()), wine -> wine));
    }

    @Override
    public boolean validateObject(Long objectId) {
        return wineRepository.existsById(objectId);
    }

    @Override
    public void afterFavorite(Long objectId, boolean isFavorite) {
        // 更新收藏计数
        wineRepository.findById(objectId).ifPresent(wine -> {
            wine.setFavoriteCount(wine.getFavoriteCount() + (isFavorite ? 1 : -1));
            wineRepository.save(wine);
        });
    }

    @Override
    public ObjectType getFavoriteType() {
        return ObjectType.WINE;
    }
}
