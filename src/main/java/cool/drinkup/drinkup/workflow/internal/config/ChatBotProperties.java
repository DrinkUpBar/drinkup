package cool.drinkup.drinkup.workflow.internal.config;

import lombok.Data;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "drinkup.chat")
public class ChatBotProperties {
    private String model = "google/gemini-2.0-flash-001";
    private String server = "openai";

    private MemoryProperties memory = new MemoryProperties();

    @Data
    public static class MemoryProperties {
        private int maxMessages = 10;
    }

    @Bean
    public ChatModel chatBotModel(OpenAiChatModel openAiChatModel, DeepSeekChatModel deepSeekChatModel) {
        if ("deepseek".equals(server)) {
            return deepSeekChatModel;
        }
        return openAiChatModel;
    }

    @Bean
    public ChatMemory chatBotChatMemory(JdbcChatMemoryRepository jdbcChatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(jdbcChatMemoryRepository)
                .maxMessages(memory.getMaxMessages())
                .build();
    }
}
