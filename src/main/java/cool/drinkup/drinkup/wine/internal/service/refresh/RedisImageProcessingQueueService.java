package cool.drinkup.drinkup.wine.internal.service.refresh;

import com.fasterxml.jackson.databind.ObjectMapper;
import cool.drinkup.drinkup.wine.internal.dto.ImageProcessingTaskDto;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisImageProcessingQueueService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final RedisKeyCleaner redisKeyCleaner;

    @Value("${redis.queue.image-processing:queue:image-processing}")
    private String processingQueueKey;

    @Value("${redis.queue.image-processing-failed:queue:image-processing-failed}")
    private String failedQueueKey;

    @Value("${redis.queue.set.processed-images:queue:set:processed-images}")
    private String processedImagesSetKey;

    @Value("${redis.queue.task.info:queue:task:info:}")
    private String taskInfoKeyPrefix;

    @Value("${redis.ttl.task-info:3600}") // 1 hour TTL for task info
    private int taskInfoTtlSeconds;

    public void addImageProcessingTask(ImageProcessingTaskDto task) {
        try {
            String taskKey = generateTaskKey(task.getEntityType(), task.getEntityId());
            String taskJson = objectMapper.writeValueAsString(task);
            String taskId = UUID.randomUUID().toString();
            task.setTaskId(taskId);
            task.setTimestamp(System.currentTimeMillis());

            if (isTaskAlreadyProcessed(task.getEntityType(), task.getEntityId())) {
                log.debug("Task already processed: {}", taskKey);
                return;
            }

            // Add task to Redis queue
            redisTemplate.opsForList().rightPush(processingQueueKey, taskJson);

            // Track task info with TTL
            String taskInfoKey = taskInfoKeyPrefix + taskId;
            redisTemplate.opsForValue().set(taskInfoKey, taskJson, Duration.ofSeconds(taskInfoTtlSeconds));

            log.info(
                    "Added image processing task to Redis queue: {} - {}:{}",
                    taskId,
                    task.getEntityType(),
                    task.getEntityId());

        } catch (Exception e) {
            log.error("Error adding task to Redis queue", e);
            throw new RuntimeException("Failed to add task to queue", e);
        }
    }

    public ImageProcessingTaskDto popImageProcessingTask() {
        try {
            String taskJson = redisTemplate.opsForList().leftPop(processingQueueKey);
            if (taskJson == null) {
                return null;
            }

            return objectMapper.readValue(taskJson, ImageProcessingTaskDto.class);

        } catch (Exception e) {
            log.error("Error popping task from Redis queue", e);
            return null;
        }
    }

    public List<ImageProcessingTaskDto> popImageProcessingTasks(int count) {
        try {
            List<String> tasksJson = redisTemplate.opsForList().leftPop(processingQueueKey, count);
            if (tasksJson == null || tasksJson.isEmpty()) {
                return List.of();
            }

            return tasksJson.stream().map(this::parseTaskFromJson).collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error popping tasks from Redis queue", e);
            return List.of();
        }
    }

    public void markTaskAsProcessed(String entityType, Long entityId) {
        String taskKey = generateTaskKey(entityType, entityId);
        redisTemplate.opsForSet().add(processedImagesSetKey, taskKey);
        log.debug("Marked task as processed: {}", taskKey);
    }

    public boolean isTaskAlreadyProcessed(String entityType, Long entityId) {
        String taskKey = generateTaskKey(entityType, entityId);
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(processedImagesSetKey, taskKey));
    }

    public void addFailedTask(ImageProcessingTaskDto task, String errorMessage) {
        try {
            task.setErrorMessage(errorMessage);
            task.setRetryCount(task.getRetryCount() + 1);
            task.setTimestamp(System.currentTimeMillis());
            String taskJson = objectMapper.writeValueAsString(task);
            redisTemplate.opsForList().rightPush(failedQueueKey, taskJson);
            log.warn("Added failed task to queue: {} - {}", task.getEntityType(), task.getEntityId());
        } catch (Exception e) {
            log.error("Error adding failed task to queue", e);
        }
    }

    public ImageProcessingTaskDto popFailedTask() {
        try {
            String taskJson = redisTemplate.opsForList().leftPop(failedQueueKey);
            if (taskJson == null) {
                return null;
            }
            return objectMapper.readValue(taskJson, ImageProcessingTaskDto.class);
        } catch (Exception e) {
            log.error("Error popping failed task from Redis queue", e);
            return null;
        }
    }

    public long getQueueLength() {
        Long size = redisTemplate.opsForList().size(processingQueueKey);
        return size != null ? size : 0;
    }

    public long getFailedQueueLength() {
        Long size = redisTemplate.opsForList().size(failedQueueKey);
        return size != null ? size : 0;
    }

    public long getProcessedTaskCount() {
        Long count = redisTemplate.opsForSet().size(processedImagesSetKey);
        return count != null ? count : 0;
    }

    public void clearQueue() {
        redisTemplate.delete(processingQueueKey);
        redisTemplate.delete(failedQueueKey);
        redisTemplate.delete(processedImagesSetKey);
        redisKeyCleaner.deleteKeysByPrefix(taskInfoKeyPrefix);
        log.info("Cleared all Redis queues and sets");
    }

    private String generateTaskKey(String entityType, Long entityId) {
        return entityType.toUpperCase() + ":" + entityId;
    }

    private ImageProcessingTaskDto parseTaskFromJson(String taskJson) {
        try {
            return objectMapper.readValue(taskJson, ImageProcessingTaskDto.class);
        } catch (Exception e) {
            log.error("Error parsing task from JSON: {}", taskJson, e);
            return null;
        }
    }
}
