package cool.drinkup.drinkup.wine.internal.service;

import cool.drinkup.drinkup.wine.internal.model.WineScene;
import cool.drinkup.drinkup.wine.internal.repository.WineSceneRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WineSceneService {

    private final WineSceneRepository sceneRepository;

    /**
     * 获取所有场景
     */
    @Cacheable(value = "wine:scenes", key = "'all'", cacheManager = "categoryCacheManager")
    public List<WineScene> getAllScenes() {
        return sceneRepository.findByIsActiveTrueOrderBySortOrderAsc();
    }

    /**
     * 根据ID获取场景
     */
    @Cacheable(value = "wine:scenes", key = "#sceneId", cacheManager = "categoryCacheManager")
    public WineScene getSceneById(Long sceneId) {
        return sceneRepository.findById(sceneId).orElseThrow(() -> new RuntimeException("场景不存在: " + sceneId));
    }
}
