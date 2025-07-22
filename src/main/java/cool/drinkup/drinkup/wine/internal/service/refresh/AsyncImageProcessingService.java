package cool.drinkup.drinkup.wine.internal.service.refresh;

import cool.drinkup.drinkup.shared.spi.ImageProcessServiceFacade;
import cool.drinkup.drinkup.shared.spi.ImageServiceFacade;
import cool.drinkup.drinkup.wine.internal.dto.ImageProcessingTaskDto;
import cool.drinkup.drinkup.wine.internal.model.UserWine;
import cool.drinkup.drinkup.wine.internal.model.Wine;
import cool.drinkup.drinkup.wine.internal.service.UserWineService;
import cool.drinkup.drinkup.wine.internal.service.WineService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AsyncImageProcessingService {

    private final RedisImageProcessingQueueService redisQueueService;
    private final ImageProcessServiceFacade imageProcessService;
    private final ImageServiceFacade imageService;
    private final WineService wineService;
    private final UserWineService userWineService;

    @Value("${image.processing.max-retries:3}")
    private int maxRetries;

    @Value("${image.processing.batch-size:10}")
    private int batchSize;

    private ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

    @Async
    @Scheduled(fixedRate = 5000, timeUnit = TimeUnit.MILLISECONDS)
    public void processBatchAsync() {
        List<ImageProcessingTaskDto> tasks = redisQueueService.popImageProcessingTasks(batchSize);

        if (tasks.isEmpty()) {
            return;
        }
        log.info("Starting async image processing batch with size: {}", batchSize);
        log.info("Processing {} image processing tasks from Redis queue with virtual threads", tasks.size());

        // Process tasks in parallel with virtual threads
        // Virtual threads handle concurrency automatically, so we don't need to limit explicitly
        @SuppressWarnings("unchecked")
        CompletableFuture<Void>[] futures = tasks.stream()
                .map(task -> CompletableFuture.runAsync(() -> processTask(task), executorService))
                .toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(futures)
                .thenRun(() -> log.info("Completed processing batch of {} tasks", tasks.size()));
        return;
    }

    @Transactional
    public void processTask(ImageProcessingTaskDto task) {
        try {
            log.info(
                    "Starting processing task: {} - {}:{}", task.getTaskId(), task.getEntityType(), task.getEntityId());

            // Skip if already processed
            if (task.getRetryCount() >= maxRetries) {
                log.warn("Task {} has reached max retries, skipping", task.getTaskId());
                redisQueueService.addFailedTask(task, "Max retries exceeded");
                return;
            }

            String imageUrl = imageService.getImageUrl(task.getImageId());
            // Process image with background removal
            String imageId = imageProcessService.removeBackgroundReturnImageId(imageUrl);

            // Mark task as processed in Redis
            redisQueueService.markTaskAsProcessed(task.getEntityType(), task.getEntityId());

            // Update the corresponding entity in database
            updateEntityProcessedImage(task.getEntityType(), task.getEntityId(), imageId);

            log.info(
                    "Successfully completed processing task: {} - {}:{}",
                    task.getTaskId(),
                    task.getEntityType(),
                    task.getEntityId());

        } catch (Exception e) {
            log.error("Error processing task: {} - {}", task.getTaskId(), task.getEntityType(), e);

            // Add to failed queue for retry
            task.setRetryCount(task.getRetryCount() + 1);
            redisQueueService.addFailedTask(task, e.getMessage());
        }
    }

    private void updateEntityProcessedImage(String entityType, Long entityId, String imageId) {
        log.info("Would update {} {} with processed image: {}", entityType, entityId, imageId);
        switch (entityType) {
            case "USER_WINE":
                UserWine userWine = userWineService.getUserWineById(entityId);
                if (userWine == null) {
                    log.warn("UserWine not found for id: {}", entityId);
                    return;
                }
                userWine.setProcessedImage(imageId);
                userWineService.save(userWine);
                break;
            case "WINE":
                Wine wine = wineService.getWineById(entityId);
                if (wine == null) {
                    log.warn("Wine not found for id: {}", entityId);
                    return;
                }
                wine.setProcessedImage(imageId);
                wineService.save(wine);
                break;
            default:
                log.warn("Unknown entity type: {}", entityType);
                break;
        }
    }

    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            log.info("Image processing virtual thread executor service shut down");
        }
    }

    public Map<String, Object> getImageProcessingStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("pendingTasks", redisQueueService.getQueueLength());
        status.put("processedTasks", redisQueueService.getProcessedTaskCount());
        status.put("failedTasks", redisQueueService.getFailedQueueLength());
        return status;
    }

    public void clearImageProcessingData() {
        redisQueueService.clearQueue();
    }
}
