package cool.drinkup.drinkup.wine.internal.service;

import cool.drinkup.drinkup.wine.internal.controller.resp.WorkflowWineVo;
import cool.drinkup.drinkup.wine.internal.mapper.WineMapper;
import cool.drinkup.drinkup.wine.internal.model.Wine;
import cool.drinkup.drinkup.wine.internal.repository.WineRepository;
import cool.drinkup.drinkup.wine.spi.WineServiceFacade;
import cool.drinkup.drinkup.wine.spi.WorkflowWineResp;
import cool.drinkup.drinkup.wine.spi.dto.ProcessCocktailRequestDto;
import jakarta.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class WineService implements WineServiceFacade {

    private final WineRepository wineRepository;
    private final WineMapper wineMapper;
    private final VectorStore vectorStore;
    private final WineCategoryService wineCategoryService;

    @Override
    @Cacheable(value = "wine", key = "#id", unless = "#result == null", cacheManager = "cacheManager")
    public @Nullable Wine getWineById(Long id) {
        return wineRepository.findById(id).orElse(null);
    }

    @Override
    public Page<Wine> getWinesByTag(String tagMainBaseSpirit, String tagIba, Pageable pageable) {
        return wineRepository.findByTagMainBaseSpiritAndTagIbaWithNullHandling(tagMainBaseSpirit, tagIba, pageable);
    }

    @Override
    public @Nullable Wine getRandomWine() {
        return wineRepository.findRandomWine();
    }

    @Override
    public List<Wine> getRandomWines(int count) {
        return wineRepository.findRandomWines(count);
    }

    @Override
    public WorkflowWineResp processCocktailRequest(ProcessCocktailRequestDto request) {
        SearchRequest.Builder searchRequestBuilder =
                SearchRequest.builder().query(request.getUserInput()).topK(10);

        // 如果有分类过滤条件，添加过滤表达式
        if (!CollectionUtils.isEmpty(request.getCategoryIds())) {
            FilterExpressionBuilder filterBuilder = new FilterExpressionBuilder();
            Object[] categoryArray = request.getCategoryIds().toArray();
            var filterExpression = filterBuilder.in("categoryId", categoryArray).build();
            searchRequestBuilder.filterExpression(filterExpression);
            log.info("Using category filter: categoryIds in {}", request.getCategoryIds());
        }

        List<Document> results = vectorStore.similaritySearch(searchRequestBuilder.build());
        log.info("Results: {}", results);

        // 按照相似度分数降序排序（分数越高，相似度越高）
        List<Document> sortedResults = results.stream()
                .sorted((doc1, doc2) -> {
                    Double score1 = doc1.getScore();
                    Double score2 = doc2.getScore();
                    if (score1 == null && score2 == null) return 0;
                    if (score1 == null) return 1; // null分数排在后面
                    if (score2 == null) return -1;
                    return Double.compare(score2, score1); // 降序排序，分数高的在前
                })
                .collect(Collectors.toList());

        log.info(
                "Sorted results by score: {}",
                sortedResults.stream()
                        .map(doc -> "wineId=" + doc.getMetadata().get("wineId") + ", score=" + doc.getScore())
                        .collect(Collectors.toList()));

        List<Long> wineIds = sortedResults.stream()
                .map(Document::getMetadata)
                .map(metadata ->
                        (long) Double.parseDouble(metadata.get("wineId").toString()))
                .distinct()
                .collect(Collectors.toList());

        List<Wine> wines = wineRepository.findAllById(wineIds);
        log.info("Wines: {}", wines);

        // 根据原始排序顺序重新排列wines列表，保持按score排序
        List<Wine> sortedWines = wineIds.stream()
                .map(wineId -> wines.stream()
                        .filter(wine -> Long.parseLong(wine.getId()) == wineId)
                        .findFirst()
                        .orElse(null))
                .filter(wine -> wine != null)
                .collect(Collectors.toList());

        log.info(
                "Sorted wines by score: {}",
                sortedWines.stream()
                        .map(wine -> "wineId=" + wine.getId() + ", name=" + wine.getName())
                        .collect(Collectors.toList()));

        List<WorkflowWineVo> workflowUserWineVos =
                sortedWines.stream().map(this::toWineVo).collect(Collectors.toList());

        WorkflowWineResp workflowUserWIneResp = new WorkflowWineResp();
        workflowUserWIneResp.setWines(workflowUserWineVos);
        return workflowUserWIneResp;
    }

    @Override
    public WorkflowWineVo toWineVo(Wine wine) {
        List<String> categoryIds = wineCategoryService.getCategoryIdsByWineId(Long.parseLong(wine.getId()));
        WorkflowWineVo workflowWineVo = wineMapper.toWineVo(wine);
        workflowWineVo.setCategoryIds(categoryIds);
        return workflowWineVo;
    }

    @Transactional
    public Wine updateWineCardImage(Long wineId, String cardImage) {
        Wine wine = wineRepository.findById(wineId).orElseThrow(() -> new RuntimeException("酒不存在，ID: " + wineId));

        wine.setCardImage(cardImage);
        return wineRepository.save(wine);
    }

    public void save(Wine wine) {
        wineRepository.save(wine);
    }
}
