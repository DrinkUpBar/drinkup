package cool.drinkup.drinkup.workflow.internal.service.agent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AgentStreamResponse {
    private String event;
    private Object data; // Changed from String to Object to support both String and JsonNode
    private String conversationId;
    private String content;
    private AgentUsage usage;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AgentUsage {
        private Integer promptTokens;
        private Integer completionTokens;
        private Integer totalTokens;
    }
}
