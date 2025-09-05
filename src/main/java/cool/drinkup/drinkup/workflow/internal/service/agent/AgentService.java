package cool.drinkup.drinkup.workflow.internal.service.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import cool.drinkup.drinkup.workflow.internal.controller.workflow.resp.WorkflowUserChatV2StreamResp;
import cool.drinkup.drinkup.workflow.internal.service.agent.dto.AgentStreamRequest;
import cool.drinkup.drinkup.workflow.internal.service.agent.dto.AgentStreamResponse;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
public class AgentService {

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${drinkup.agent.base-url:http://drinkup-agent}")
    private String agentBaseUrl;

    @Value("${drinkup.agent.connect-timeout:30000}")
    private int connectTimeout;

    @Value("${drinkup.agent.read-timeout:300000}")
    private int readTimeout;

    /**
     * 调用 Python Agent 流式接口 - 使用 HttpURLConnection 实现真正的 SSE 流式处理
     */
    public Flux<String> chatStream(AgentStreamRequest request) {
        log.info(
                "Calling Python Agent stream API - userId: {}, conversationId: {}",
                request.getUserId(),
                request.getConversationId());

        return Flux.create(sink -> {
                    HttpURLConnection connection = null;
                    try {
                        String requestJson = objectMapper.writeValueAsString(request);

                        // 使用 HttpURLConnection 实现 SSE 流式读取
                        URI uri = URI.create(agentBaseUrl + "/api/workflow/chat/v2/stream");
                        URL url = uri.toURL();
                        connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("POST");
                        connection.setRequestProperty("Content-Type", "application/json");
                        connection.setRequestProperty("Accept", "text/event-stream");
                        connection.setDoOutput(true);
                        connection.setDoInput(true);
                        connection.setConnectTimeout(connectTimeout);
                        connection.setReadTimeout(readTimeout);

                        // 发送请求体 - 使用 try-with-resources 自动关闭输出流
                        try (var outputStream = connection.getOutputStream()) {
                            outputStream.write(requestJson.getBytes(StandardCharsets.UTF_8));
                            outputStream.flush();
                        }

                        // 检查响应码
                        int responseCode = connection.getResponseCode();
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            // 读取 SSE 流 - 使用 try-with-resources 自动管理资源
                            try (InputStream inputStream = connection.getInputStream();
                                    BufferedReader reader = new BufferedReader(
                                            new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

                                StringBuilder eventBuffer = new StringBuilder();
                                String line;

                                while ((line = reader.readLine()) != null) {
                                    if (line.isEmpty()) {
                                        // 空行表示一个事件结束
                                        if (eventBuffer.length() > 0) {
                                            String eventData = eventBuffer.toString();
                                            log.debug("Received SSE event: {}", eventData);
                                            sink.next(eventData);
                                            eventBuffer = new StringBuilder();
                                        }
                                    } else {
                                        // 累积事件数据
                                        eventBuffer.append(line).append("\n");
                                    }
                                }

                                // 处理最后可能的事件
                                if (eventBuffer.length() > 0) {
                                    sink.next(eventBuffer.toString());
                                }
                            }

                            sink.complete();

                        } else {
                            // 读取错误流以获取更多错误信息
                            String errorMessage = "";
                            try (InputStream errorStream = connection.getErrorStream()) {
                                if (errorStream != null) {
                                    try (BufferedReader errorReader = new BufferedReader(
                                            new InputStreamReader(errorStream, StandardCharsets.UTF_8))) {
                                        errorMessage = errorReader.lines().reduce("", (a, b) -> a + "\n" + b);
                                    }
                                }
                            } catch (Exception e) {
                                log.debug("Could not read error stream", e);
                            }

                            log.error(
                                    "Failed to connect to Python Agent, response code: {}," + " error: {}",
                                    responseCode,
                                    errorMessage);
                            sink.error(new RuntimeException(
                                    "Failed to connect to Python Agent: " + responseCode + ", error: " + errorMessage));
                        }

                    } catch (Exception e) {
                        log.error("Error calling Python Agent", e);
                        sink.error(e);
                    } finally {
                        // 确保连接被断开
                        if (connection != null) {
                            try {
                                connection.disconnect();
                            } catch (Exception e) {
                                log.debug("Error disconnecting connection", e);
                            }
                        }
                    }
                })
                .<String>cast(String.class)
                .subscribeOn(Schedulers.fromExecutor(Executors.newVirtualThreadPerTaskExecutor())); // 使用虚拟线程执行，提高并发性能
    }

    /**
     * 解析 Server-Sent Events 数据
     */
    public AgentStreamResponse parseSSEData(String rawData) {
        try {
            // 解析 SSE 格式: event: xxx\ndata: {...}
            String[] lines = rawData.split("\n");
            String event = null;
            String data = null;

            for (String line : lines) {
                if (line.startsWith("event: ")) {
                    event = line.substring(7).trim();
                } else if (line.startsWith("data: ")) {
                    data = line.substring(6).trim();
                }
            }

            if (event != null) {
                AgentStreamResponse response = new AgentStreamResponse();
                response.setEvent(event);

                // 对于没有data字段的事件（如纯event通知），也返回response
                if (data != null && !data.isEmpty() && !data.equals("null")) {
                    try {
                        // 尝试解析为JSON对象
                        com.fasterxml.jackson.databind.JsonNode jsonNode = objectMapper.readTree(data);
                        response.setData(jsonNode);
                    } catch (Exception jsonEx) {
                        // 如果不是JSON，就作为字符串保存
                        response.setData(data);
                    }
                } else {
                    // 对于streaming_content事件，可能data为单个字符
                    response.setData(data != null ? data : "");
                }

                return response;
            }

        } catch (Exception e) {
            log.error("Error parsing SSE data: {}", rawData, e);
        }

        return null;
    }

    /**
     * 将 Agent 响应转换为流式响应格式
     */
    public WorkflowUserChatV2StreamResp convertToStreamResponse(AgentStreamResponse agentResponse) {
        var builder = WorkflowUserChatV2StreamResp.builder()
                .event(agentResponse.getEvent())
                .conversationId(agentResponse.getConversationId());

        switch (agentResponse.getEvent()) {
            case "streaming_content":
                // 处理streaming_content事件，data可能是字符串或JSON对象
                Object data = agentResponse.getData();
                if (data instanceof String) {
                    // 如果data是字符串，直接作为content
                    builder.content((String) data);
                } else if (data != null) {
                    // 如果data是JSON对象，尝试提取content字段
                    var contentData = objectMapper.convertValue(data, com.fasterxml.jackson.databind.JsonNode.class);
                    if (contentData.has("content")) {
                        builder.content(contentData.get("content").asText());
                    } else {
                        // 如果没有content字段，将整个data作为字符串
                        builder.content(contentData.asText());
                    }
                }
                break;

            case "agent_thinking":
                var thinkingData = objectMapper.convertValue(
                        agentResponse.getData(), com.fasterxml.jackson.databind.JsonNode.class);
                if (thinkingData != null && thinkingData.has("tool_calls")) {
                    var toolCallsArray = thinkingData.get("tool_calls");
                    var toolCalls = new WorkflowUserChatV2StreamResp.ToolCall[toolCallsArray.size()];
                    for (int i = 0; i < toolCallsArray.size(); i++) {
                        var toolCall = toolCallsArray.get(i);
                        toolCalls[i] = WorkflowUserChatV2StreamResp.ToolCall.builder()
                                .name(
                                        toolCall.has("name")
                                                ? toolCall.get("name").asText()
                                                : null)
                                .args(
                                        toolCall.has("args")
                                                ? objectMapper.convertValue(toolCall.get("args"), Object.class)
                                                : null)
                                .build();
                    }
                    builder.toolCalls(toolCalls);
                }
                break;

            case "tool_result":
                var toolResultData = objectMapper.convertValue(
                        agentResponse.getData(), com.fasterxml.jackson.databind.JsonNode.class);
                if (toolResultData != null) {
                    builder.toolResult(WorkflowUserChatV2StreamResp.ToolResult.builder()
                            .tool(
                                    toolResultData.has("tool")
                                            ? toolResultData.get("tool").asText()
                                            : null)
                            .result(
                                    toolResultData.has("result")
                                            ? toolResultData.get("result").asText()
                                            : null)
                            .build());
                }
                break;

            case "final_response":
                var finalData = objectMapper.convertValue(
                        agentResponse.getData(), com.fasterxml.jackson.databind.JsonNode.class);
                if (finalData != null) {
                    var usageBuilder = WorkflowUserChatV2StreamResp.Usage.builder();
                    if (finalData.has("usage")) {
                        var usage = finalData.get("usage");
                        usageBuilder
                                .promptTokens(
                                        usage.has("prompt_tokens")
                                                ? usage.get("prompt_tokens").asInt()
                                                : null)
                                .completionTokens(
                                        usage.has("completion_tokens")
                                                ? usage.get("completion_tokens").asInt()
                                                : null)
                                .totalTokens(
                                        usage.has("total_tokens")
                                                ? usage.get("total_tokens").asInt()
                                                : null);
                    }

                    builder.finalResponse(WorkflowUserChatV2StreamResp.FinalResponse.builder()
                            .conversationId(
                                    finalData.has("conversation_id")
                                            ? finalData.get("conversation_id").asText()
                                            : null)
                            .content(
                                    finalData.has("content")
                                            ? finalData.get("content").asText()
                                            : null)
                            .usage(usageBuilder.build())
                            .build());
                }
                break;

            case "error":
                var errorData = objectMapper.convertValue(
                        agentResponse.getData(), com.fasterxml.jackson.databind.JsonNode.class);
                if (errorData != null) {
                    builder.error(WorkflowUserChatV2StreamResp.ErrorInfo.builder()
                            .message(
                                    errorData.has("message")
                                            ? errorData.get("message").asText()
                                            : (errorData.has("error")
                                                    ? errorData.get("error").asText()
                                                    : "Unknown error"))
                            .conversationId(
                                    errorData.has("conversation_id")
                                            ? errorData.get("conversation_id").asText()
                                            : null)
                            .build());
                }
                break;

            default:
                // 未知事件类型，保留原始数据
                builder.data(agentResponse.getData());
                break;
        }

        return builder.build();
    }
}
