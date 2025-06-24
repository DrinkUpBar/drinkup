package cool.drinkup.drinkup.workflow.internal.service.chat;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.ResponseFormat;
import org.springframework.aop.framework.AopContext;
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

import cool.drinkup.drinkup.common.chatLog.annotation.AiLog;
import cool.drinkup.drinkup.workflow.internal.config.ChatBotProperties;
import cool.drinkup.drinkup.workflow.internal.controller.workflow.req.WorkflowUserChatReq.WorkflowUserChatVo;
import cool.drinkup.drinkup.workflow.internal.enums.PromptTypeEnum;
import cool.drinkup.drinkup.workflow.internal.model.PromptContent;
import cool.drinkup.drinkup.workflow.internal.repository.PromptRepository;
import cool.drinkup.drinkup.workflow.internal.service.chat.dto.ChatParams;
import cool.drinkup.drinkup.workflow.internal.service.chat.dto.ChatParams.ImageAttachment;
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

    public ChatBotService(@Qualifier("chatBotModel") ChatModel chatModel,
            @Qualifier("chatBotChatMemory") ChatMemory chatMemory, ImageService imageService,
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

    public record ChatBotResponse(String conversationId, String content) {
    }

    @Observed(name = "ai.chat", contextualName = "AI聊天", lowCardinalityKeyValues = {
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
        var systemMessage = buildSystemMessage(params);
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

    @Observed(name = "ai.chat", contextualName = "AI聊天", lowCardinalityKeyValues = {
            "Tag", "ai"
    })
    public ChatBotResponse chatV2(String conversationId, String userContent, ChatParams params) {
        if (!StringUtils.hasText(conversationId)) {
            conversationId = UUID.randomUUID().toString();
            SystemMessage message = buildSystemMessage(params);
            this.chatMemory.add(conversationId, message);
        }
        var userMessageList = buildUserMessage(userContent, params);
        this.chatMemory.add(conversationId, userMessageList);
        Prompt prompt = buildPrompt(conversationId, params);
        ChatBotService proxy = (ChatBotService) AopContext.currentProxy();
        var response = proxy.aiChatV2(conversationId, prompt);
        this.chatMemory.add(conversationId, response.getResult().getOutput());
        log.info("ai response : {}", response);
        String text = response.getResult().getOutput().getText();
        log.info("Chat response: {}", text);
        return new ChatBotResponse(conversationId, text);
    }

    @AiLog(conversationId = "#conversationId")
    public ChatResponse aiChatV2(String conversationId, Prompt prompt) {
        return chatModel.call(prompt);
    }

    private SystemMessage buildSystemMessage(ChatParams params) {
        String systemPrompt = promptTemplate.replace("${userStock}", params.getUserStock());
        return new SystemMessage(systemPrompt);
    }

    /**
             * 动态更新指定对话中的 SystemMessage
     * 
     * @param conversationId 对话ID
     * @param params         包含新的 userStock 的参数
     */
    public List<Message> updateSystemMessage(String conversationId, ChatParams params) {
        List<Message> messages = chatMemory.get(conversationId);
        if (messages == null || messages.isEmpty()) {
            log.warn("对话 {} 不存在或为空，无法更新 SystemMessage", conversationId);
            return messages;
        }

        // 查找并替换第一个 SystemMessage
        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i) instanceof SystemMessage) {
                SystemMessage newSystemMessage = buildSystemMessage(params);
                messages.set(i, newSystemMessage);
                log.info("已更新对话 {} 的 SystemMessage，新的 userStock: {}", conversationId, params.getUserStock());
                return messages;
            }
        }

        log.warn("对话 {} 中未找到 SystemMessage", conversationId);
        return messages;
    }

    public Prompt buildPrompt(String conversationId, ChatParams params) {
        List<Message> messages = updateSystemMessage(conversationId, params);

        return new Prompt(messages,
                OpenAiChatOptions.builder()
                        .model(chatBotProperties.getModel())
                        .responseFormat(ResponseFormat.builder()
                                .type(ResponseFormat.Type.JSON_OBJECT).build())
                        .build());
    }

    private List<Message> buildUserMessage(String userInput, ChatParams params) {
        if (params.getImageAttachmentList() != null && !params.getImageAttachmentList().isEmpty()) {
            List<Message> userMessages = new ArrayList<>();
            userMessages.add(UserMessage.builder().text(userInput).build());
            for (ImageAttachment imageAttachment : params.getImageAttachmentList()) {
                Resource resource = imageService.loadImage(imageAttachment.getImageId());
                try {
                    String mime = contentTypeUtil.detectMimeType(resource);
                    Media media = new Media(MimeType.valueOf(mime), resource);
                    userMessages.add(UserMessage.builder()
                            .text("This is the image of the user's uploaded. image id is "
                                    + imageAttachment.getImageId())
                            .media(List.of(media)).build());
                } catch (IOException e) {
                    log.error("Error loading image: {}", e.getMessage(), e);
                    throw new RuntimeException("Image load error");
                }
            }
            return userMessages;
        } else {
            return List.of(UserMessage.builder().text(userInput).build());
        }
    }

}
