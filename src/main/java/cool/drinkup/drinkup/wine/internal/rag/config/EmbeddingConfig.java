package cool.drinkup.drinkup.wine.internal.rag.config;

import lombok.Data;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.BatchingStrategy;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@ConfigurationProperties(prefix = "drinkup.embedding")
@Data
public class EmbeddingConfig {

    private Model openai = new Model();

    @Data
    public static class Model {
        private String baseUrl;
        private String apiKey;
        private String model;
    }

    @Bean
    BatchingStrategy customBatchingStrategy() {
        return new CustomBatchingStrategy();
    }

    @Bean
    @Primary
    EmbeddingModel customOpenAiEmbeddingModel() {
        OpenAiApi openAiApi = OpenAiApi.builder()
                .baseUrl(openai.getBaseUrl())
                .apiKey(openai.getApiKey())
                .build();
        OpenAiEmbeddingOptions options =
                OpenAiEmbeddingOptions.builder().model(openai.model).build();
        OpenAiEmbeddingModel model = new OpenAiEmbeddingModel(openAiApi, MetadataMode.EMBED, options);
        return model;
    }
}
