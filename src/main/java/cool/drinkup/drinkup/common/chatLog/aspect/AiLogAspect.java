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
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Tracer tracer;
    private final AuthenticationServiceFacade authenticationServiceFacade;
    private final Executor threadPoolTaskExecutor = Executors.newVirtualThreadPerTaskExecutor();

    @Around("@annotation(aiLog)")
    public Object around(ProceedingJoinPoint joinPoint, AiLog aiLog) throws Throwable {

        Instant start = Instant.now();
        Object result = null;

        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();

        // 校验方法返回类型
        Class<?> returnType = method.getReturnType();
        if (!ChatResponse.class.isAssignableFrom(returnType)) {
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
        } finally {
            Instant end = Instant.now();
            String requestInput = objectMapper.writeValueAsString(prompt);
            String responseOutput = objectMapper.writeValueAsString(result);
            ChatOptions chatOptions = prompt.getOptions();
            ChatResponse chatResponse = (ChatResponse) result;
            threadPoolTaskExecutor.execute(() -> {
                aiChatLogRepository.save(AiChatLog.builder()
                        .timestamp(Instant.now())
                        .traceId(tracer.currentSpan().context().traceId())
                        .conversationId(conversationId)
                        .requestInput(requestInput)
                        .responseOutput(responseOutput)
                        .latency(Duration.between(start, end).toMillis())
                        .modelName(chatOptions.getModel())
                        .userId(authenticationServiceFacade.getCurrentAuthenticatedUser()
                                .map(AuthenticatedUserDTO::userId).orElse(null))
                        .status(status)
                        .errorMessage(errorMessage)
                        .promptTokens(chatResponse.getMetadata().getUsage().getPromptTokens())
                        .completionTokens(chatResponse.getMetadata().getUsage().getCompletionTokens())
                        .totalTokens(chatResponse.getMetadata().getUsage().getTotalTokens())
                        .build());
            });

        }

        return result;
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
        for (Object arg : args) {
            if (arg instanceof Prompt) {
                return (Prompt) arg;
            }
        }
        return null;
    }
}
