package cool.drinkup.drinkup.workflow.internal.service.agent.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AgentStreamRequest {
    @JsonProperty("user_message")
    private String userMessage;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("conversation_id")
    private String conversationId;

    @JsonProperty("params")
    private AgentParams params;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AgentParams {
        @JsonProperty("user_stock")
        private String userStock;

        @JsonProperty("user_info")
        private String userInfo;

        @JsonProperty("image_attachment_list")
        private java.util.List<ImageAttachmentDto> imageAttachmentList;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ImageAttachmentDto {
        @JsonProperty("image_base64")
        private String imageBase64;

        @JsonProperty("mime_type")
        private String mimeType;
    }
}
