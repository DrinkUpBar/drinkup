package cool.drinkup.drinkup.common.log.repository.impl;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "log_record")
public class LogRecord {

    /**
     * 日志记录ID
     */
    @Id
    private String id;

    /**
     * 租户标识
     */
    @Field(type = FieldType.Keyword)
    private String tenant;

    /**
     * 日志类型
     */
    @Field(type = FieldType.Keyword)
    private String type;

    /**
     * 日志子类型
     */
    @Field(type = FieldType.Keyword)
    private String subType;

    /**
     * 业务编号
     */
    @Field(type = FieldType.Keyword)
    private String bizNo;

    /**
     * 操作人
     */
    @Field(type = FieldType.Keyword)
    private String operator;

    /**
     * 操作动作描述
     */
    @Field(type = FieldType.Text)
    private String action;

    /**
     * 是否失败
     */
    @Field(type = FieldType.Boolean)
    private Boolean fail;

    /**
     * 创建时间
     */
    @Builder.Default
    @Field(type = FieldType.Date)
    private ZonedDateTime createTime = ZonedDateTime.now(ZoneOffset.UTC);

    /**
     * 额外信息
     */
    @Field(type = FieldType.Text)
    private String extra;

    /**
     * 代码变量信息
     */
    @Field(type = FieldType.Object)
    private CodeVariable codeVariable;

    /**
     * 代码变量内嵌类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CodeVariable {
        /**
         * 类名
         */
        @Field(type = FieldType.Keyword)
        private String className;

        /**
         * 方法名
         */
        @Field(type = FieldType.Keyword)
        private String methodName;
    }
}
