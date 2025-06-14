package cool.drinkup.drinkup.workflow.internal.config;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "drinkup.translate")
public class AITranslateProperties {
    private String model = "google/gemini-2.0-flash-001";
    private String server = "openai";

    @Bean
    public ChatModel translateChatModel(OpenAiChatModel openAiChatModel, DeepSeekChatModel deepSeekChatModel) {
        if ("deepseek".equals(server)) {
            return deepSeekChatModel;
        }
        return openAiChatModel;
    }
}
