package cool.drinkup.drinkup.wine.internal.rag;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cool.drinkup.drinkup.wine.internal.model.Wine;
import cool.drinkup.drinkup.wine.internal.model.WineCategoryMapping;
import cool.drinkup.drinkup.wine.internal.repository.WineCategoryMappingRepository;
import cool.drinkup.drinkup.wine.internal.repository.WineRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataLoaderService {

    private final VectorStore vectorStore;
    private final WineRepository wineRepository;
    private final WineCategoryMappingRepository wineCategoryMappingRepository;
    private ObjectMapper objectMapper = new ObjectMapper();

    public void loadData() {
        List<Wine> wines = wineRepository.findAll();
        for (Wine wine : wines) {
            log.info("Loading wine: {}, {}", wine.getId(), wine.getName());
            List<Document> documents = new ArrayList<>();
            String jsonString;
            try {
                jsonString = objectMapper.writeValueAsString(wine);

                // Get category mappings for this wine
                List<WineCategoryMapping> categoryMappings =
                        wineCategoryMappingRepository.findByWineId(Long.valueOf(wine.getId()));
                List<Long> categoryIds = categoryMappings.stream()
                        .map(WineCategoryMapping::getCategoryId)
                        .toList();

                if (CollectionUtils.isEmpty(categoryIds)) {
                    Map<String, Object> metadata = Map.of("wineId", wine.getId());
                    Document document = new Document(jsonString, metadata);
                    documents.add(document);
                } else {
                    for (Long categoryId : categoryIds) {
                        Map<String, Object> metadata = Map.of("wineId", wine.getId(), "categoryId", categoryId);
                        Document document = new Document(jsonString, metadata);
                        documents.add(document);
                    }
                }
                vectorStore.add(documents);
            } catch (Exception e) {
                log.error("Error converting wine to JSON string: {}", e.getMessage());
                continue;
            }
        }
    }

    public void addData(Long wineId) {
        // Clear existing data for this wine ID first
        clearDataByWineId(wineId);

        Wine wine = wineRepository.findById(wineId).orElseThrow(() -> new RuntimeException("Wine not found"));
        String jsonString;
        try {
            jsonString = objectMapper.writeValueAsString(wine);

            // Get category mappings for this wine
            List<WineCategoryMapping> categoryMappings = wineCategoryMappingRepository.findByWineId(wineId);
            List<Long> categoryIds = categoryMappings.stream()
                    .map(WineCategoryMapping::getCategoryId)
                    .toList();

            List<Document> documents = new ArrayList<>();

            if (CollectionUtils.isEmpty(categoryIds)) {
                Map<String, Object> metadata = Map.of("wineId", wine.getId());
                Document document = new Document(jsonString, metadata);
                documents.add(document);
            } else {
                for (Long categoryId : categoryIds) {
                    Map<String, Object> metadata = Map.of("wineId", wine.getId(), "categoryId", categoryId);
                    Document document = new Document(jsonString, metadata);
                    documents.add(document);
                }
            }

            vectorStore.add(documents);
        } catch (JsonProcessingException e) {
            log.error("Error converting wine to JSON string: {}", e.getMessage());
            throw new RuntimeException("Failed to add wine data for wine ID: " + wineId, e);
        }
    }

    /**
     * 清除向量数据库中的所有数据
     * 该方法使用多种策略确保数据被成功清除：
     * 1. 使用批量获取并删除（推荐方式）
     * 2. 使用过滤器表达式删除（回退方案）
     */
    public void clearData() {
        log.info("Starting vector store clear operation");

        try {
            // 方法1：批量获取并删除ID（推荐方式，因为更可靠）
            log.info("Attempting to clear vector store using batch deletion");
            clearDataWithBatchDeletion();
            log.info("Vector store cleared successfully using batch deletion");

        } catch (Exception e) {
            log.warn("Batch deletion failed, attempting filter-based deletion: {}", e.getMessage());

            // 方法2：使用过滤器删除（回退方案）
            try {
                clearDataWithFilterDeletion();
                log.info("Vector store cleared successfully using filter-based deletion");
            } catch (Exception fallbackEx) {
                log.error("All clearing methods failed. Error: {}", fallbackEx.getMessage(), fallbackEx);
                throw new RuntimeException(
                        "Unable to clear vector store after trying all available methods", fallbackEx);
            }
        }

        log.info("Vector store clear operation completed successfully");
    }

    /**
     * 使用过滤器表达式删除数据的方法
     */
    private void clearDataWithFilterDeletion() {
        log.info("Starting filter-based deletion method");

        // 尝试多种过滤器表达式以确保删除所有数据
        boolean deleted = false;

        // 尝试1: 使用 Milvus 支持的过滤器语法
        try {
            vectorStore.delete("wineId >= 0");
            log.info("Successfully deleted documents using 'wineId >= 0' filter");
            deleted = true;
        } catch (Exception filterEx1) {
            log.warn("Filter 'wineId >= 0' failed: {}", filterEx1.getMessage());
        }

        if (!deleted) {
            // 尝试2: 删除所有文档的通用表达式
            try {
                vectorStore.delete("pk >= 0"); // pk 是 Milvus 的主键
                log.info("Successfully deleted documents using 'pk >= 0' filter");
                deleted = true;
            } catch (Exception filterEx2) {
                log.warn("Filter 'pk >= 0' failed: {}", filterEx2.getMessage());
            }
        }

        if (!deleted) {
            // 尝试3: 尝试空字符串过滤器（某些实现支持）
            try {
                vectorStore.delete("");
                log.info("Successfully deleted documents using empty filter");
                deleted = true;
            } catch (Exception filterEx3) {
                log.warn("Empty filter failed: {}", filterEx3.getMessage());
            }
        }

        if (!deleted) {
            throw new RuntimeException("All filter-based deletion attempts failed");
        }
    }

    /**
     * 使用批量删除的方式清除向量数据库
     * 这是推荐的删除方式，因为更加可靠
     */
    private void clearDataWithBatchDeletion() {
        log.info("Starting batch deletion method");
        int batchSize = 100; // 减小批次大小以提高稳定性
        boolean hasMore = true;
        int totalDeleted = 0;
        int maxAttempts = 200; // 增加最大尝试次数
        int attempts = 0;
        int consecutiveEmptyResults = 0; // 连续空结果计数

        while (hasMore && attempts < maxAttempts) {
            attempts++;

            try {
                // 尝试多种查询方式来获取文档
                List<Document> documents = null;

                // 方式1: 使用空查询
                try {
                    documents = vectorStore.similaritySearch(SearchRequest.builder()
                            .query("") // 空查询应该返回所有文档
                            .topK(batchSize)
                            .build());
                } catch (Exception e1) {
                    log.warn("Empty query failed, trying alternative query: {}", e1.getMessage());

                    // 方式2: 使用通用查询词
                    try {
                        documents = vectorStore.similaritySearch(SearchRequest.builder()
                                .query("wine") // 使用与文档内容相关的查询词
                                .topK(batchSize)
                                .build());
                    } catch (Exception e2) {
                        log.warn("Wine query failed, trying wildcard query: {}", e2.getMessage());

                        // 方式3: 使用通配符查询
                        try {
                            documents = vectorStore.similaritySearch(SearchRequest.builder()
                                    .query("*") // 通配符查询
                                    .topK(batchSize)
                                    .build());
                        } catch (Exception e3) {
                            log.warn("All query methods failed: {}", e3.getMessage());
                            throw e3;
                        }
                    }
                }

                if (documents == null || documents.isEmpty()) {
                    consecutiveEmptyResults++;
                    log.info(
                            "No documents found (attempt {}), consecutive empty results: {}",
                            attempts,
                            consecutiveEmptyResults);

                    // 如果连续多次获取空结果，认为删除完成
                    if (consecutiveEmptyResults >= 3) {
                        log.info("Consecutive empty results reached threshold, deletion complete");
                        hasMore = false;
                    } else {
                        // 短暂等待后重试，可能是数据同步延迟
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException("Interrupted during batch deletion", ie);
                        }
                    }
                } else {
                    consecutiveEmptyResults = 0; // 重置连续空结果计数
                    List<String> ids = documents.stream()
                            .map(Document::getId)
                            .filter(id -> id != null && !id.trim().isEmpty())
                            .toList();

                    if (!ids.isEmpty()) {
                        vectorStore.delete(ids);
                        totalDeleted += ids.size();
                        log.info(
                                "Deleted batch of {} documents, total deleted: {} (attempt {})",
                                ids.size(),
                                totalDeleted,
                                attempts);
                    }

                    // 如果返回的文档数少于批次大小，可能接近完成
                    if (documents.size() < batchSize) {
                        log.info(
                                "Retrieved {} documents (less than batch size {}), may be near" + " completion",
                                documents.size(),
                                batchSize);
                    }
                }

            } catch (Exception batchEx) {
                log.error("Error during batch deletion at attempt {}: {}", attempts, batchEx.getMessage());
                consecutiveEmptyResults = 0; // 重置计数

                if (attempts >= 5) { // 连续失败5次后放弃
                    throw new RuntimeException("Batch deletion failed after " + attempts + " attempts", batchEx);
                }
                // 短暂等待后重试
                try {
                    Thread.sleep(1000 + (attempts * 500)); // 递增等待时间
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during batch deletion", ie);
                }
            }
        }

        if (attempts >= maxAttempts) {
            log.warn("Reached maximum attempts ({}) for batch deletion, stopping", maxAttempts);
        }

        // 最终验证
        try {
            List<Document> finalCheck = vectorStore.similaritySearch(
                    SearchRequest.builder().query("").topK(1).build());
            if (!finalCheck.isEmpty()) {
                log.warn("Final verification found {} remaining documents", finalCheck.size());
            } else {
                log.info("Final verification confirmed: no documents remaining");
            }
        } catch (Exception finalEx) {
            log.warn("Final verification failed: {}", finalEx.getMessage());
        }

        log.info("Batch deletion completed. Total deleted: {} documents in {} attempts", totalDeleted, attempts);
    }

    /**
     * 检查向量存储的当前状态
     * 用于诊断和调试
     */
    public void checkVectorStoreStatus() {
        log.info("Checking vector store status...");

        try {
            // 尝试获取少量文档来检查存储状态
            List<Document> documents = vectorStore.similaritySearch(
                    SearchRequest.builder().query("").topK(5).build());

            log.info("Found {} documents in vector store", documents.size());

            if (!documents.isEmpty()) {
                log.info(
                        "Sample document IDs: {}",
                        documents.stream().map(Document::getId).limit(3).toList());

                // 检查元数据
                Document firstDoc = documents.get(0);
                if (firstDoc.getMetadata() != null && !firstDoc.getMetadata().isEmpty()) {
                    log.info("Sample metadata keys: {}", firstDoc.getMetadata().keySet());
                }
            }

        } catch (Exception e) {
            log.error("Error checking vector store status: {}", e.getMessage(), e);
        }
    }

    /**
     * 清除指定酒单ID的向量数据
     * @param wineId 要清除的酒单ID
     */
    public void clearDataByWineId(Long wineId) {
        log.info("Starting to clear vector store data for wine ID: {}", wineId);

        try {
            // 使用过滤器删除指定酒单ID的所有文档
            String filter = "wineId == " + wineId;
            vectorStore.delete(filter);
            log.info("Successfully cleared data for wine ID: {}", wineId);

        } catch (Exception e) {
            log.error("Failed to clear data for wine ID {}: {}", wineId, e.getMessage(), e);
            throw new RuntimeException("Failed to clear existing data for wine ID: " + wineId, e);
        }
    }

    /**
     * 获取向量存储中的文档总数估计
     */
    public int getDocumentCount() {
        try {
            List<Document> documents = vectorStore.similaritySearch(SearchRequest.builder()
                    .query("")
                    .topK(10000) // 设置一个较大的值来估算总数
                    .build());

            log.info("Estimated document count: {}", documents.size());
            return documents.size();

        } catch (Exception e) {
            log.error("Error getting document count: {}", e.getMessage());
            return -1;
        }
    }
}
