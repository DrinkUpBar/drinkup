package cool.drinkup.drinkup.wine.internal.service.refresh;

import cool.drinkup.drinkup.wine.internal.dto.ImageProcessingTaskDto;
import cool.drinkup.drinkup.wine.internal.model.UserWine;
import cool.drinkup.drinkup.wine.internal.model.Wine;
import cool.drinkup.drinkup.wine.internal.repository.UserWineRepository;
import cool.drinkup.drinkup.wine.internal.repository.WineRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageProcessingTaskService {

    private final UserWineRepository userWineRepository;
    private final WineRepository wineRepository;
    private final RedisImageProcessingQueueService redisQueueService;

    public int createImageProcessingTasks(int batchSize) {
        log.info("Starting to create image processing tasks, batch size: {}", batchSize);
        int createdTasks = 0;

        // Process UserWine entities
        List<UserWine> userWines = userWineRepository.findAll();
        for (UserWine userWine : userWines) {
            if (shouldProcessImage(userWine.getImage(), userWine.getProcessedImage())) {
                Long userId = Long.valueOf(userWine.getId());
                if (!redisQueueService.isTaskAlreadyProcessed("USER_WINE", userId)) {
                    ImageProcessingTaskDto task = new ImageProcessingTaskDto("USER_WINE", userId, userWine.getImage());
                    redisQueueService.addImageProcessingTask(task);
                    createdTasks++;
                    if (createdTasks >= batchSize) {
                        break;
                    }
                }
            }
        }

        // Process Wine entities if we still have capacity
        if (createdTasks < batchSize) {
            List<Wine> wines = wineRepository.findAll();
            for (Wine wine : wines) {
                if (shouldProcessImage(wine.getImage(), wine.getProcessedImage())) {
                    Long wineId = Long.valueOf(wine.getId());
                    if (!redisQueueService.isTaskAlreadyProcessed("WINE", wineId)) {
                        ImageProcessingTaskDto task = new ImageProcessingTaskDto("WINE", wineId, wine.getImage());
                        redisQueueService.addImageProcessingTask(task);
                        createdTasks++;
                        if (createdTasks >= batchSize) {
                            break;
                        }
                    }
                }
            }
        }

        log.info("Created {} image processing tasks in Redis queue", createdTasks);
        return createdTasks;
    }

    public long getPendingTaskCount() {
        return redisQueueService.getQueueLength();
    }

    private boolean shouldProcessImage(String imageUrl, String processedImageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return false;
        }
        if (processedImageUrl != null && !processedImageUrl.trim().isEmpty()) {
            return false;
        }
        return true;
    }
}
