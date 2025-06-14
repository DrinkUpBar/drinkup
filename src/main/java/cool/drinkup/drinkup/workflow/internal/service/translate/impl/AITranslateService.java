package cool.drinkup.drinkup.workflow.internal.service.translate.impl;

import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.List;

import cool.drinkup.drinkup.workflow.internal.enums.PromptTypeEnum;
import cool.drinkup.drinkup.workflow.internal.model.PromptContent;
import cool.drinkup.drinkup.workflow.internal.repository.PromptRepository;
import cool.drinkup.drinkup.workflow.internal.service.translate.TranslateService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AITranslateService implements TranslateService {
    
    private final ChatModel translateChatModel;

    private final PromptRepository promptRepository;

    private String promptTemplate;

    @PostConstruct
    public void init() {
        PromptContent prompt = promptRepository.findByType(PromptTypeEnum.TRANSLATE.name());
        if (prompt == null) {
            return;
        }
        this.promptTemplate = prompt.getSystemPrompt();
    }

    @Override
    public String translate(String text, String targetLanguage, String scene) {
        if (promptTemplate == null) {
            log.warn("翻译提示词模板未找到，使用默认翻译");
            return text;
        }
        
        // 构建系统提示词，替换占位符
        String systemPrompt = promptTemplate
            .replace("{text}", text)
            .replace("{targetLanguage}", targetLanguage)
            .replace("{scene}", scene != null ? scene : "通用场景");
        
        // 构建 Prompt
        var systemMessage = new SystemMessage(systemPrompt);
        var userMessage = new UserMessage("请翻译以上内容到" + targetLanguage);
        var prompt = new Prompt(List.of(systemMessage, userMessage));
        
        try {
            // 调用 AI 模型进行翻译
            var response = translateChatModel.call(prompt);
            String translatedText = response.getResult().getOutput().getText();
            log.info("翻译完成: {} -> {}", text, translatedText);
            return translatedText;
        } catch (Exception e) {
            log.error("翻译失败: {}", e.getMessage(), e);
            return text; // 翻译失败时返回原文
        }
    }
}
