package cool.drinkup.drinkup.wine.internal.service;

import cool.drinkup.drinkup.wine.internal.controller.resp.WorkflowWineVo;
import cool.drinkup.drinkup.wine.internal.mapper.WineMapper;
import cool.drinkup.drinkup.wine.internal.model.Wine;
import cool.drinkup.drinkup.wine.internal.repository.WineRepository;
import cool.drinkup.drinkup.wine.spi.WineServiceFacade;
import cool.drinkup.drinkup.wine.spi.WorkflowWineResp;
import jakarta.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WineService implements WineServiceFacade {

    private final WineRepository wineRepository;
    private final WineMapper wineMapper;
    private final VectorStore vectorStore;

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
    public WorkflowWineResp processCocktailRequest(String userInput) {
        List<Document> results = vectorStore.similaritySearch(
                SearchRequest.builder().query(userInput).topK(10).build());
        log.info("Results: {}", results);
        List<Long> wineIds = results.stream()
                .map(Document::getMetadata)
                .map(metadata ->
                        (long) Double.parseDouble(metadata.get("wineId").toString()))
                .collect(Collectors.toList());
        List<Wine> wines = wineRepository.findAllById(wineIds);
        log.info("Wines: {}", wines);
        List<WorkflowWineVo> workflowUserWineVos =
                wines.stream().map(wineMapper::toWineVo).collect(Collectors.toList());
        WorkflowWineResp workflowUserWIneResp = new WorkflowWineResp();
        workflowUserWIneResp.setWines(workflowUserWineVos);
        return workflowUserWIneResp;
    }

    @Override
    public WorkflowWineVo toWineVo(Wine wine) {
        return wineMapper.toWineVo(wine);
    }

    @Transactional
    public Wine updateWineCardImage(Long wineId, String cardImage) {
        Wine wine = wineRepository.findById(wineId).orElseThrow(() -> new RuntimeException("酒不存在，ID: " + wineId));

        wine.setCardImage(cardImage);
        return wineRepository.save(wine);
    }
}
