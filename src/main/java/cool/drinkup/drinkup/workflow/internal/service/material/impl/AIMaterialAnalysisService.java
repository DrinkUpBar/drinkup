package cool.drinkup.drinkup.workflow.internal.service.material.impl;

import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.List;

import cool.drinkup.drinkup.workflow.internal.enums.PromptTypeEnum;
import cool.drinkup.drinkup.workflow.internal.model.PromptContent;
import cool.drinkup.drinkup.workflow.internal.repository.PromptRepository;
import cool.drinkup.drinkup.workflow.internal.service.material.MaterialAnalysisService;
import cool.drinkup.drinkup.workflow.internal.service.material.MaterialService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AIMaterialAnalysisService implements MaterialAnalysisService {

    private final MaterialService materialService;

    private final ChatModel materialAnalysisChatModel;

    private final PromptRepository promptRepository;

    private String promptTemplate;

    @PostConstruct
    public void init() {
        PromptContent prompt = promptRepository.findByType(PromptTypeEnum.MATERIAL_ANALYSIS.name()); 
        if (prompt == null) {
            return;
        }
        this.promptTemplate = prompt.getSystemPrompt();
    }

    @Override
    public MaterialAnalysisResult analyzeMaterial(String materialText) {
        Prompt prompt = buildPrompt(materialText);
        var response = materialAnalysisChatModel.call(prompt);
        String description = response.getResult().getOutput().getText();
        return new MaterialAnalysisResult(description);
    }

    private Prompt buildPrompt(String materialText) {
        String systemPrompt = promptTemplate.replace("{material_text}", materialText);
        var systemMessage = new SystemMessage(systemPrompt);
        var userMessage = new UserMessage("Analyze the material ");
        return new Prompt(List.of(systemMessage, userMessage));
    }

}
