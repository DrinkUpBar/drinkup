package cool.drinkup.drinkup.wine.internal.rag.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.BatchingStrategy;

public class CustomBatchingStrategy implements BatchingStrategy {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<List<Document>> batch(List<Document> documents) {
        List<List<Document>> batches = new ArrayList<>();

        for (Document document : documents) {
            String text = document.getText();
            List<Document> processedDocuments = new ArrayList<>();
            try {
                // Try to parse the text as JSON array
                List<?> jsonArray = objectMapper.readValue(text, List.class);
                // If successful, create a new document for each array element
                for (Object element : jsonArray) {
                    String elementText = objectMapper.writeValueAsString(element);
                    Document newDoc = new Document(elementText, document.getMetadata());
                    processedDocuments.add(newDoc);
                }
            } catch (JsonProcessingException e) {
                // If not a JSON array, keep the original document
                processedDocuments.add(document);
            }
            batches.add(processedDocuments);
        }
        // Return the processed documents as a single batch
        return batches;
    }
}
