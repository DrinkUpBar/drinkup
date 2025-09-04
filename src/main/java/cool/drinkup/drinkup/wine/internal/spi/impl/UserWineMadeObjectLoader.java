package cool.drinkup.drinkup.wine.internal.spi.impl;

import cool.drinkup.drinkup.favorite.spi.MadeObjectLoader;
import cool.drinkup.drinkup.favorite.spi.ObjectType;
import cool.drinkup.drinkup.wine.internal.model.UserWine;
import cool.drinkup.drinkup.wine.internal.repository.UserWineRepository;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 用户酒单已调制对象加载器实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserWineMadeObjectLoader implements MadeObjectLoader<UserWine> {

    private final UserWineRepository userWineRepository;

    @Override
    public Map<Long, UserWine> loadObjects(List<Long> objectIds) {
        if (objectIds == null || objectIds.isEmpty()) {
            return Map.of();
        }

        List<UserWine> userWines = userWineRepository.findAllById(objectIds);
        return userWines.stream()
                .collect(Collectors.toMap(userWine -> Long.valueOf(userWine.getId()), Function.identity()));
    }

    @Override
    public boolean validateObject(Long objectId) {
        if (objectId == null) {
            return false;
        }
        return userWineRepository.existsById(objectId);
    }

    @Override
    public void afterMade(Long objectId, boolean isMade) {
        if (objectId == null) {
            log.warn("UserWine ID is null, skipping afterMade callback");
            return;
        }

        log.info("UserWine {} made status changed to: {}", objectId, isMade);
        // 可以在这里添加其他业务逻辑，比如更新统计信息、通知用户等
    }

    @Override
    public ObjectType getMadeType() {
        return ObjectType.USER_WINE;
    }
}
