package cool.drinkup.drinkup.workflow.internal.config;

import lombok.Data;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "drinkup.bartender")
public class BartenderProperties {
    private String model = "deepseek/deepseek-chat-v3-0324";
    private Double temperature = 0.8;
    private String server = "openai";

    private RecoverableBartenderProperties recoverable = new RecoverableBartenderProperties();

    @Data
    public static class RecoverableBartenderProperties {
        private String model = "deepseek/deepseek-chat-v3-0324";
        private Double temperature = 0.8;
        private String server = "openai";
    }

    @Bean
    public ChatModel bartenderChatModel(OpenAiChatModel openAiChatModel, DeepSeekChatModel deepSeekChatModel) {
        if ("deepseek".equals(server)) {
            return deepSeekChatModel;
        }
        return openAiChatModel;
    }

    @Bean
    public ChatModel bartenderRecoverableChatModel(
            OpenAiChatModel openAiChatModel, DeepSeekChatModel deepSeekChatModel) {
        if ("deepseek".equals(server)) {
            return deepSeekChatModel;
        }
        return openAiChatModel;
    }
}
