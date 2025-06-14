package cool.drinkup.drinkup.workflow.internal.service.chat;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.ResponseFormat;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import cool.drinkup.drinkup.workflow.internal.config.ChatBotProperties;
import cool.drinkup.drinkup.workflow.internal.controller.workflow.req.WorkflowUserChatReq.WorkflowUserChatVo;
import cool.drinkup.drinkup.workflow.internal.enums.PromptTypeEnum;
import cool.drinkup.drinkup.workflow.internal.model.PromptContent;
import cool.drinkup.drinkup.workflow.internal.repository.PromptRepository;
import cool.drinkup.drinkup.workflow.internal.service.chat.dto.ChatParams;
import cool.drinkup.drinkup.workflow.internal.service.image.ImageService;
import cool.drinkup.drinkup.workflow.internal.util.ContentTypeUtil;
import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ChatBotService {

    private final ChatModel chatModel;

    private final ImageService imageService;

    private final ContentTypeUtil contentTypeUtil;

    private final ChatBotProperties chatBotProperties;

    private final PromptRepository promptRepository;

    private final ChatMemory chatMemory;

    private String promptTemplate;

    public ChatBotService(@Qualifier("chatBotModel") ChatModel chatModel, @Qualifier("chatBotChatMemory") ChatMemory chatMemory, ImageService imageService,
        ContentTypeUtil contentTypeUtil, ChatBotProperties chatBotProperties, PromptRepository promptRepository) {
        this.chatModel = chatModel;
        this.imageService = imageService;
        this.contentTypeUtil = contentTypeUtil;
        this.chatBotProperties = chatBotProperties;
        this.promptRepository = promptRepository;
        this.chatMemory = chatMemory;
    }

    @PostConstruct
    public void init() {
        PromptContent prompt = promptRepository.findByType(PromptTypeEnum.CHAT.name());
        if (prompt == null) {
            return;
        }
        this.promptTemplate = prompt.getSystemPrompt();
    }

    public record ChatResponse(String conversationId, String content) {
    }

    @Observed(name = "ai.chat",
        contextualName = "AI聊天",
        lowCardinalityKeyValues = {
            "Tag", "ai"
        })
    public String chat(List<WorkflowUserChatVo> messages, ChatParams params) {
        var prompt = buildPrompt(messages, params);
        var response = chatModel.call(prompt);
        log.info("ai response : {}", response);
        String text = response.getResult().getOutput().getText();
        log.info("Chat response: {}", text);
        return text;
    }

    private Prompt buildPrompt(List<WorkflowUserChatVo> messages, ChatParams params) {
        String systemPrompt = promptTemplate.replace("{userStock}", params.getUserStock());

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
            .collect(Collectors.toList());

        var allMessages = new ArrayList<Message>();
        allMessages.add(systemMessage);
        allMessages.addAll(historyMessages);
        if (params.getImageId() != null) {
            Resource resource = imageService.loadImage(params.getImageId());
            try {
                String mime = contentTypeUtil.detectMimeType(resource);
                Media media = new Media(MimeType.valueOf(mime), resource);
                UserMessage userMessage = UserMessage.builder().text("This is the image of the user's stock: ").media(
                    List.of(media)).build();
                allMessages.add(userMessage);
            } catch (IOException e) {
                log.error("Error loading image: {}", e.getMessage());
            }
        }

        return new Prompt(allMessages, OpenAiChatOptions.builder()
            .model(chatBotProperties.getModel())
            .responseFormat(ResponseFormat.builder().type(ResponseFormat.Type.JSON_OBJECT).build())
            .build());
    }

    @Observed(name = "ai.chat",
        contextualName = "AI聊天",
        lowCardinalityKeyValues = {
            "Tag", "ai"
        })
    public ChatResponse chatV2(String conversationId, String userContent, ChatParams params) {
        if (!StringUtils.hasText(conversationId)) {
            conversationId = UUID.randomUUID().toString();
            this.chatMemory.add(conversationId, new SystemMessage(promptTemplate));
        }
        var userMessage = buildUserMessage(userContent, params);
        this.chatMemory.add(conversationId, userMessage);
        Prompt prompt = buildPrompt(conversationId);
        var response = chatModel.call(prompt);
        this.chatMemory.add(conversationId, response.getResult().getOutput());
        log.info("ai response : {}", response);
        String text = response.getResult().getOutput().getText();
        log.info("Chat response: {}", text);
        return new ChatResponse(conversationId, text);
    }

    public Prompt buildPrompt(String conversationId) {
        return new Prompt(chatMemory.get(conversationId),
            OpenAiChatOptions.builder()
                .model(chatBotProperties.getModel())
                .responseFormat(ResponseFormat.builder()
                    .type(ResponseFormat.Type.JSON_OBJECT).build())
                .build());
    }

    private UserMessage buildUserMessage(String userInput, ChatParams params) {
        if (params.getImageId() != null) {
            Resource resource = imageService.loadImage(params.getImageId());
            try {
                String mime = contentTypeUtil.detectMimeType(resource);
                Media media = new Media(MimeType.valueOf(mime), resource);
                return UserMessage.builder().text("This is the image of the user's stock: ").media(List.of(media)).build();
            } catch (IOException e) {
                log.error("Error loading image: {}", e.getMessage(), e);
                throw new RuntimeException("Image load error");
            }
        } else {
            return UserMessage.builder().text(userInput).build();
        }
    }

}
