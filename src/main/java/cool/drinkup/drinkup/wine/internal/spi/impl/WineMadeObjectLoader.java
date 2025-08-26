package cool.drinkup.drinkup.wine.internal.spi.impl;

import cool.drinkup.drinkup.favorite.spi.MadeObjectLoader;
import cool.drinkup.drinkup.favorite.spi.ObjectType;
import cool.drinkup.drinkup.wine.internal.model.Wine;
import cool.drinkup.drinkup.wine.internal.repository.WineRepository;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 酒单已调制对象加载器实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WineMadeObjectLoader implements MadeObjectLoader<Wine> {

    private final WineRepository wineRepository;

    @Override
    public Map<Long, Wine> loadObjects(List<Long> objectIds) {
        if (objectIds == null || objectIds.isEmpty()) {
            return Map.of();
        }
        List<Wine> wines = wineRepository.findAllById(objectIds);
        return wines.stream().collect(Collectors.toMap(wine -> Long.valueOf(wine.getId()), Function.identity()));
    }

    @Override
    public boolean validateObject(Long objectId) {
        if (objectId == null) {
            return false;
        }
        return wineRepository.existsById(objectId);
    }

    @Override
    public void afterMade(Long objectId, boolean isMade) {
        if (objectId == null) {
            log.warn("Wine ID is null, skipping afterMade callback");
            return;
        }

        log.info("Wine {} made status changed to: {}", objectId, isMade);
        // 可以在这里添加其他业务逻辑，比如更新统计信息等
    }

    @Override
    public ObjectType getMadeType() {
        return ObjectType.WINE;
    }
}
