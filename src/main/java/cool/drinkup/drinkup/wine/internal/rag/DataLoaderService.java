package cool.drinkup.drinkup.wine.internal.rag;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cool.drinkup.drinkup.wine.internal.model.Wine;
import cool.drinkup.drinkup.wine.internal.repository.WineRepository;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataLoaderService {

    private final VectorStore vectorStore;
    private final WineRepository wineRepository;
    private ObjectMapper objectMapper = new ObjectMapper();

    public void loadData() {
        List<Wine> wines = wineRepository.findAll();
        for (Wine wine : wines) {
            String jsonString;
            try {
                jsonString = objectMapper.writeValueAsString(wine);
                Document document = new Document(wine.getId().toString(), jsonString, Map.of("wineId", wine.getId()));
                vectorStore.add(List.of(document));
            } catch (JsonProcessingException e) {
                log.error("Error converting wine to JSON string: {}", e.getMessage());
                continue;
            }
        }
    }

    public void addData(Long wineId) {
        Wine wine = wineRepository.findById(wineId).orElseThrow(() -> new RuntimeException("Wine not found"));
        String jsonString;
        try {
            jsonString = objectMapper.writeValueAsString(wine);
            Document document = new Document(wineId.toString(), jsonString, Map.of("wineId", wine.getId()));
            vectorStore.add(List.of(document));
        } catch (JsonProcessingException e) {
            log.error("Error converting wine to JSON string: {}", e.getMessage());
        }
    }

    /**
     * 清除向量数据库中的所有数据
     * 该方法使用多种策略确保数据被成功清除：
     * 1. 使用过滤器表达式删除（推荐方式）
     * 2. 批量获取ID并删除（回退方案）
     */
    public void clearData() {
        log.info("Starting vector store clear operation");

        try {
            // 方法1：使用过滤器删除所有包含wineId元数据的文档（推荐）
            log.info("Attempting to clear vector store using filter-based deletion");

            // 尝试多种过滤器表达式以确保删除所有数据
            try {
                // 删除所有包含wineId元数据的文档
                vectorStore.delete("wineId >= 0");
                log.info("Successfully deleted documents using 'wineId >= 0' filter");
            } catch (Exception filterEx1) {
                log.warn("Filter 'wineId >= 0' failed, trying alternative filter: {}", filterEx1.getMessage());
                try {
                    // 尝试删除所有文档（如果支持通用过滤器）
                    vectorStore.delete("id >= 0");
                    log.info("Successfully deleted documents using 'id >= 0' filter");
                } catch (Exception filterEx2) {
                    log.warn("Filter 'id >= 0' also failed: {}", filterEx2.getMessage());
                    throw filterEx2; // 抛出异常以进入回退方案
                }
            }

            log.info("Vector store cleared successfully using filter-based deletion");

        } catch (Exception e) {
            log.warn("Filter-based deletion failed, attempting fallback method: {}", e.getMessage());

            // 方法2：批量获取并删除ID（回退方案）
            try {
                clearDataWithBatchDeletion();
            } catch (Exception fallbackEx) {
                log.error("All clearing methods failed. Error: {}", fallbackEx.getMessage(), fallbackEx);
                throw new RuntimeException(
                        "Unable to clear vector store after trying all available methods", fallbackEx);
            }
        }

        log.info("Vector store clear operation completed successfully");
    }

    /**
     * 使用批量删除的方式清除向量数据库
     * 这是一个回退方案，当过滤器删除失败时使用
     */
    private void clearDataWithBatchDeletion() {
        log.info("Starting batch deletion fallback method");
        int batchSize = 1000;
        boolean hasMore = true;
        int totalDeleted = 0;
        int maxAttempts = 100; // 防止无限循环
        int attempts = 0;

        while (hasMore && attempts < maxAttempts) {
            attempts++;

            try {
                List<Document> documents = vectorStore.similaritySearch(SearchRequest.builder()
                        .query("") // 空查询应该返回所有文档
                        .topK(batchSize)
                        .build());

                if (documents.isEmpty()) {
                    log.info("No more documents found, deletion complete");
                    hasMore = false;
                } else {
                    List<String> ids = documents.stream().map(Document::getId).toList();

                    vectorStore.delete(ids);
                    totalDeleted += ids.size();
                    log.info(
                            "Deleted batch of {} documents, total deleted: {} (attempt {})",
                            ids.size(),
                            totalDeleted,
                            attempts);

                    // 如果返回的文档数少于批次大小，说明可能是最后一批
                    if (documents.size() < batchSize) {
                        // 进行一次额外检查确保没有遗漏
                        try {
                            List<Document> remainingDocs = vectorStore.similaritySearch(SearchRequest.builder()
                                    .query("")
                                    .topK(10) // 小批次检查
                                    .build());
                            if (remainingDocs.isEmpty()) {
                                hasMore = false;
                            }
                        } catch (Exception checkEx) {
                            log.warn(
                                    "Failed to verify remaining documents, assuming deletion" + " complete: {}",
                                    checkEx.getMessage());
                            hasMore = false;
                        }
                    }
                }

            } catch (Exception batchEx) {
                log.error("Error during batch deletion at attempt {}: {}", attempts, batchEx.getMessage());
                if (attempts >= 3) { // 连续失败3次后放弃
                    throw new RuntimeException("Batch deletion failed after " + attempts + " attempts", batchEx);
                }
                // 短暂等待后重试
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during batch deletion", ie);
                }
            }
        }

        if (attempts >= maxAttempts) {
            log.warn("Reached maximum attempts ({}) for batch deletion, stopping", maxAttempts);
        }

        log.info("Batch deletion completed. Total deleted: {} documents in {} attempts", totalDeleted, attempts);
    }
}
