package cool.drinkup.drinkup.common.chatLog.model;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "ai_chat_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatLog {

    @Id
    private String id;

    @Field(type = FieldType.Date)
    private Instant timestamp;

    @Field(type = FieldType.Keyword)
    private String traceId;

    @Field(type = FieldType.Keyword)
    private String conversationId;

    @Field(type = FieldType.Keyword)
    private Long userId;

    @Field(type = FieldType.Text)
    private String requestInput;

    @Field(type = FieldType.Text)
    private String responseOutput;

    @Field(type = FieldType.Keyword)
    private String modelName;

    @Field(type = FieldType.Long)
    private Long latency;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Text)
    private String errorMessage;

    @Field(type = FieldType.Integer)
    private Integer promptTokens;

    @Field(type = FieldType.Integer)
    private Integer completionTokens;

    @Field(type = FieldType.Integer)
    private Integer totalTokens;
}
