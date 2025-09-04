package cool.drinkup.drinkup.workflow.internal.controller.workflow.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkflowUserChatV2StreamResp {
    private String event;
    private String conversationId;
    private Object data;

    // 针对不同事件类型的具体数据
    private String content; // streaming_content 事件
    private ToolCall[] toolCalls; // agent_thinking 事件
    private ToolResult toolResult; // tool_result 事件
    private FinalResponse finalResponse; // final_response 事件
    private ErrorInfo error; // error 事件

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ToolCall {
        private String name;
        private Object args;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ToolResult {
        private String tool;
        private String result;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FinalResponse {
        private String conversationId;
        private String content;
        private Usage usage;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Usage {
        private Integer promptTokens;
        private Integer completionTokens;
        private Integer totalTokens;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ErrorInfo {
        private String message;
        private String conversationId;
    }
}
