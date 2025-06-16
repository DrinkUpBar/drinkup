package cool.drinkup.drinkup.common.chatLog.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import cool.drinkup.drinkup.common.chatLog.annotation.AiLog;
import cool.drinkup.drinkup.common.chatLog.model.AiChatLog;
import cool.drinkup.drinkup.common.chatLog.repository.AiChatLogRepository;
import cool.drinkup.drinkup.user.spi.AuthenticatedUserDTO;
import cool.drinkup.drinkup.user.spi.AuthenticationServiceFacade;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AiLogAspect {

    private final AiChatLogRepository aiChatLogRepository;
    private final ExpressionParser parser = new SpelExpressionParser();
    private final ObjectMapper objectMapper = createConfiguredObjectMapper();
    private final Tracer tracer;
    private final AuthenticationServiceFacade authenticationServiceFacade;
    private final Executor threadPoolTaskExecutor = Executors.newVirtualThreadPerTaskExecutor();

    private ObjectMapper createConfiguredObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // 添加 JSR310 模块以支持 Java 8 时间类型
        mapper.registerModule(new JavaTimeModule());
        // 禁用写入日期为时间戳
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 在序列化失败时忽略不支持的字段
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        return mapper;
    }

    @Around("@annotation(aiLog)")
    public Object around(ProceedingJoinPoint joinPoint, AiLog aiLog) throws Throwable {

        Instant start = Instant.now();
        Object result = null;

        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();

        // 校验方法返回类型
        Class<?> returnType = method.getReturnType();
        if ( !ChatResponse.class.isAssignableFrom(returnType)) {
            log.error("AiLog 注解只能用于返回类型为 String 或 ChatResponse 的方法，当前方法: {}", method.getName());
            return joinPoint.proceed();
        }
        Prompt prompt = extractPrompt(joinPoint);
        if ( prompt == null) {
            log.error("AiLog 注解只能用于返回类型为 String 或 ChatResponse 的方法，当前方法: {}", method.getName());
            return joinPoint.proceed();
        }
        Object[] args = joinPoint.getArgs();
        EvaluationContext context = new MethodBasedEvaluationContext(null, method, args,
                new DefaultParameterNameDiscoverer());

        String conversationId = parseSpEL(aiLog.conversationId(), context);
        String errorMessage = null;
        String status = "success";

        try {
            result = joinPoint.proceed();
            context.setVariable("_ret", result); // 供 completion 表达式使用
        } catch (Throwable e) {
            throw e;
        } finally {
            Object finalResult = result;
            Prompt finalPrompt = prompt;
            Instant finalStart = start;
            String finalConversationId = conversationId;
            String finalStatus = status;
            String finalErrorMessage = errorMessage;
            // 安全获取 traceId
            Span currentSpan = tracer.currentSpan();
            String traceId = currentSpan != null ? currentSpan.context().traceId() : null;
            Long userId = authenticationServiceFacade.getCurrentAuthenticatedUser()
                    .map(AuthenticatedUserDTO::userId).orElse(null);
            threadPoolTaskExecutor.execute(() -> {
                try {
                    Instant end = Instant.now();
                    String requestInput = safeSerialize(finalPrompt);
                    String responseOutput = safeSerialize(finalResult);
                    ChatOptions chatOptions = finalPrompt.getOptions();
                    ChatResponse chatResponse = (ChatResponse) finalResult;

                    aiChatLogRepository.save(AiChatLog.builder()
                            .timestamp(Instant.now())
                            .traceId(traceId)
                            .conversationId(finalConversationId)
                            .requestInput(requestInput)
                            .responseOutput(responseOutput)
                            .latency(Duration.between(finalStart, end).toMillis())
                            .modelName(chatOptions.getModel())
                            .userId(userId)
                            .status(finalStatus)
                            .errorMessage(finalErrorMessage)
                            .promptTokens(chatResponse.getMetadata().getUsage().getPromptTokens())
                            .completionTokens(chatResponse.getMetadata().getUsage().getCompletionTokens())
                            .totalTokens(chatResponse.getMetadata().getUsage().getTotalTokens())
                            .build());
                } catch (Exception e) {
                    log.error("Failed to save AI chat log", e);
                }
            });

        }

        return result;
    }

    private String safeSerialize(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("Failed to serialize object: {}", e.getMessage());
            return "Serialization failed: " + e.getMessage();
        }
    }

    private String parseSpEL(String spel, EvaluationContext context) {
        if ( spel == null || spel.isBlank())
            return null;
        try {
            Expression exp = parser.parseExpression(spel);
            Object value = exp.getValue(context);
            return value != null ? value.toString() : null;
        } catch (Exception e) {
            log.warn("SpEL解析失败: [{}] -> {}", spel, e.getMessage());
            return null;
        }
    }

    private Prompt extractPrompt(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        for ( Object arg : args) {
            if ( arg instanceof Prompt) {
                return (Prompt) arg;
            }
        }
        return null;
    }
}
