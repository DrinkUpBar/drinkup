package cool.drinkup.drinkup.workflow.internal.service.bartender;

import org.apache.commons.text.StringSubstitutor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.ResponseFormat;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import cool.drinkup.drinkup.common.chatLog.annotation.AiLog;
import cool.drinkup.drinkup.workflow.internal.config.BartenderProperties;
import cool.drinkup.drinkup.workflow.internal.controller.workflow.req.WorkflowBartenderChatReq.WorkflowBartenderChatVo;
import cool.drinkup.drinkup.workflow.internal.enums.PromptTypeEnum;
import cool.drinkup.drinkup.workflow.internal.exception.RetryException;
import cool.drinkup.drinkup.workflow.internal.model.PromptContent;
import cool.drinkup.drinkup.workflow.internal.repository.PromptRepository;
import cool.drinkup.drinkup.workflow.internal.service.bartender.dto.BartenderParams;
import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BartenderService {

    private final ChatModel chatModel;

    private final ChatModel recoverableChatModel;

    private String promptTemplate;

    private final BartenderProperties bartenderProperties;

    private final PromptRepository promptRepository;

    private final ChatMemory chatMemory;

    public BartenderService(@Qualifier("bartenderChatModel") ChatModel chatModel, @Qualifier("bartenderRecoverableChatModel") ChatModel recoverableChatModel,
        BartenderProperties bartenderProperties, PromptRepository promptRepository, ChatMemory chatMemory)
        throws IOException {
        this.chatModel = chatModel;
        this.recoverableChatModel = recoverableChatModel;
        this.bartenderProperties = bartenderProperties;
        this.promptRepository = promptRepository;
        this.chatMemory = chatMemory;
    }

    @PostConstruct
    public void init() {
        PromptContent prompt = promptRepository.findByType(PromptTypeEnum.BARTENDER.name());
        if (prompt == null) {
            return;
        }
        this.promptTemplate = prompt.getSystemPrompt();
    }

    @Retryable(
        value = {RuntimeException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000)
    )
    @Observed(name = "ai.bartender.chat",
        contextualName = "Bartender聊天",
        lowCardinalityKeyValues = {
            "Tag", "ai"
        })
    public String generateDrinkV2(String conversationId, BartenderParams bartenderParams) {
        try {
            var prompt = buildPromptV2(conversationId, bartenderParams);
            BartenderService proxy = (BartenderService) AopContext.currentProxy();
            var response = proxy.bartenderChatV2(conversationId, prompt);
            log.info("bartender response: {}", response);

            if (response.getResult() == null) {
                log.error("AI response result is null. Response details: {}", response);
                throw new RuntimeException("AI response result is null");
            }

            String text = response.getResult().getOutput().getText();
            log.info("Chat response: {}", text);
            return text;
        } catch (Exception e) {
            log.error("Error generating drink recommendation", e);
            throw new RetryException("Error generating drink recommendation");
        }
    }

    @AiLog(conversationId = "#conversationId")
    public ChatResponse bartenderChatV2(String conversationId, Prompt prompt) {
        return chatModel.call(prompt);
    }

    @Recover
    @Observed(name = "ai.bartender.chat",
        contextualName = "Bartender聊天重试",
        lowCardinalityKeyValues = {
            "Tag", "ai"
        })
    public String generateDrinkRecoverableV2(RetryException exception, String conversationId, BartenderParams bartenderParams) {
        try {
            var prompt = buildPromptV2(conversationId, bartenderParams);
            BartenderService proxy = (BartenderService) AopContext.currentProxy();
            var response = proxy.bartenderChatV2(conversationId, prompt);
            log.info("bartender response: {}", response);
            if (response.getResult() == null) {
                log.error("AI response result is null. Response details: {}", response);
                throw new RuntimeException("AI response result is null");
            }

            String text = response.getResult().getOutput().getText();
            log.info("Chat response: {}", text);
            return text;
        } catch (Exception e) {
            log.error("Error generating drink recommendation", e);
            throw new RuntimeException("Error generating drink recommendation after retry");
        }
    }


    private Prompt buildPromptV2(String conversationId, BartenderParams bartenderParams) {
        if (!StringUtils.hasText(conversationId)) {
            conversationId = UUID.randomUUID().toString();
            Map<String, String> substituterMap = bartenderParams.toSubstituterMap();
            StringSubstitutor substitutor = new StringSubstitutor(substituterMap);
            String systemPrompt = substitutor.replace(promptTemplate);
            var systemMessage = new SystemMessage(systemPrompt);
            this.chatMemory.add(conversationId, systemMessage);
        }
        return new Prompt(this.chatMemory.get(conversationId),
            OpenAiChatOptions.builder()
                .model(bartenderProperties.getModel())
                .temperature(bartenderProperties.getTemperature())
                .responseFormat(
                    ResponseFormat
                        .builder()
                        .type(ResponseFormat.Type.JSON_OBJECT)
                        .build())
                .build());
    }

    @Deprecated
    @Retryable(
        value = {RuntimeException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000)
    )
    @Observed(name = "ai.bartender.chat",
        contextualName = "Bartender聊天",
        lowCardinalityKeyValues = {
            "Tag", "ai"
        })
    public String generateDrink(List<WorkflowBartenderChatVo> messages, BartenderParams bartenderParams) {
        try {
            var prompt = buildPrompt(messages, bartenderParams);
            var response = chatModel.call(prompt);
            log.info("bartender response: {}", response);

            if (response.getResult() == null) {
                log.error("AI response result is null. Response details: {}", response);
                throw new RuntimeException("AI response result is null");
            }

            String text = response.getResult().getOutput().getText();
            log.info("Chat response: {}", text);
            return text;
        } catch (Exception e) {
            log.error("Error generating drink recommendation", e);
            throw new RetryException("Error generating drink recommendation");
        }
    }

    @Deprecated
    @Recover
    @Observed(name = "ai.bartender.chat",
        contextualName = "Bartender聊天重试",
        lowCardinalityKeyValues = {
            "Tag", "ai"
        })
    public String generateDrinkRecoverable(RetryException exception, List<WorkflowBartenderChatVo> messages, BartenderParams bartenderParams) {
        try {
            var prompt = buildPrompt(messages, bartenderParams);
            var response = recoverableChatModel.call(prompt);
            log.info("bartender response: {}", response);

            if (response.getResult() == null) {
                log.error("AI response result is null. Response details: {}", response);
                throw new RuntimeException("AI response result is null");
            }

            String text = response.getResult().getOutput().getText();
            log.info("Chat response: {}", text);
            return text;
        } catch (Exception e) {
            log.error("Error generating drink recommendation", e);
            throw new RuntimeException("Error generating drink recommendation after retry");
        }
    }

    private Prompt buildPrompt(List<WorkflowBartenderChatVo> messages, BartenderParams bartenderParams) {
        Map<String, String> substituterMap = bartenderParams.toSubstituterMap();
        StringSubstitutor substitutor = new StringSubstitutor(substituterMap);
        String systemPrompt = substitutor.replace(promptTemplate);

        var systemMessage = new SystemMessage(systemPrompt);
        var historyMessages = messages.stream()
            .map(message -> {
                if (message.getRole().equals("user")) {
                    return new UserMessage(message.getContent());
                } else if (message.getRole().equals("assistant")) {
                    return new AssistantMessage(message.getContent());
                } else {
                    throw new IllegalArgumentException("Invalid message role: " + message.getRole());
                }
            })
            .toList();

        var allMessages = new ArrayList<Message>();
        allMessages.add(systemMessage);
        allMessages.addAll(historyMessages);

        return new Prompt(allMessages, OpenAiChatOptions.builder()
            .model(bartenderProperties.getModel())
            .temperature(bartenderProperties.getTemperature())
            .responseFormat(
                ResponseFormat
                    .builder()
                    .type(ResponseFormat.Type.JSON_OBJECT)
                    .build())
            .build());
    }

}
