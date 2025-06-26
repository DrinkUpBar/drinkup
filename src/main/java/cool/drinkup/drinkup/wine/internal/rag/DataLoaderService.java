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
}
