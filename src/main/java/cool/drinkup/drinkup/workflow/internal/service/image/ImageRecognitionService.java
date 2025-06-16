package cool.drinkup.drinkup.workflow.internal.service.image;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

import cool.drinkup.drinkup.workflow.internal.controller.bar.req.BarStockCreateReq;
import cool.drinkup.drinkup.workflow.internal.controller.bar.req.BarStockCreateReq.InnerBarStockCreateReq;
import cool.drinkup.drinkup.workflow.internal.enums.PromptTypeEnum;
import cool.drinkup.drinkup.workflow.internal.model.BarStock;
import cool.drinkup.drinkup.workflow.internal.model.PromptContent;
import cool.drinkup.drinkup.workflow.internal.repository.PromptRepository;
import cool.drinkup.drinkup.workflow.internal.util.ContentTypeUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class ImageRecognitionService {
    @Qualifier("openAiChatModel")
    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;
    private final ImageService imageService;
    private final ContentTypeUtil contentTypeUtil;
    private final PromptRepository promptRepository;
    private String promptTemplate;

    @Value("${drinkup.image.recognition.model:google/gemini-2.0-flash-001}")
    private String model;

    public ImageRecognitionService(@Qualifier("openAiChatModel") ChatModel chatModel,
            ObjectMapper objectMapper, ImageService imageService, ContentTypeUtil contentTypeUtil, PromptRepository promptRepository) {
        this.chatModel = chatModel;
        this.objectMapper = objectMapper;
        this.imageService = imageService;
        this.contentTypeUtil = contentTypeUtil;
        this.promptRepository = promptRepository;
    }

    @PostConstruct
    public void init() {
        PromptContent prompt = promptRepository.findByType(PromptTypeEnum.IMAGE_RECOGNITION.name());
        if (prompt == null) {
            return; 
        }
        this.promptTemplate = prompt.getSystemPrompt();
    }

    public record StreamBarStockResult(boolean isDone, String text, List<BarStock> barStocks) {
    }

    public Flux<StreamBarStockResult> recognizeStockFromImageStream(String imageId) {
        try {
            if (promptTemplate == null || promptTemplate.trim().isEmpty()) {
                log.error("Prompt template is not initialized");
                return Flux.just(new StreamBarStockResult(true, "Error: Prompt template not found", 
                        new ArrayList<>()));
            }
            
            List<Message> messages = new ArrayList<>();
            messages.add(new SystemMessage(promptTemplate));
            Resource image = imageService.loadImage(imageId);
            String mimeType = contentTypeUtil.detectMimeType(image).toString();
            UserMessage userMessage = UserMessage.builder().text("这是原料图片，请开始识别")
                    .media(List.of(new Media(MimeType.valueOf(mimeType), image))).build();
            messages.add(userMessage);
            Prompt prompt = new Prompt(messages);

            // 创建一个共享的flux来避免重复调用AI模型
            Flux<ChatResponse> sharedResponseFlux = chatModel.stream(prompt).share();

            // 获取流式的中间结果
            Flux<StreamBarStockResult> streamingResults = sharedResponseFlux
                    .scan(new StringBuilder(), (acc, chatResponse) -> {
                        // 提取当前chunk的文本并累积
                        if (chatResponse.getResult() != null && chatResponse.getResult().getOutput() != null) {
                            String text = chatResponse.getResult().getOutput().getText();
                            if (text != null && !text.isEmpty()) {
                                acc.append(text);
                            }
                        }
                        return acc;
                    })
                    .skip(1) // 跳过初始的空StringBuilder
                    .map(accumulatedText -> {
                        // 返回实时的累积结果，isDone=false表示还在streaming
                        return new StreamBarStockResult(false, accumulatedText.toString(), null);
                    });

            // 获取最终结果
            Mono<StreamBarStockResult> finalResult = sharedResponseFlux
                    .reduce(new StringBuilder(), (acc, chatResponse) -> {
                        if (chatResponse.getResult() != null && chatResponse.getResult().getOutput() != null) {
                            String text = chatResponse.getResult().getOutput().getText();
                            if (text != null) {
                                acc.append(text);
                            }
                        }
                        return acc;
                    })
                    .map(fullText -> {
                        List<BarStock> barStocks = parseRecognitionResponse(fullText.toString());
                        return new StreamBarStockResult(true, fullText.toString(), barStocks);
                    });

            // 合并流式结果和最终结果
            return streamingResults
                    .concatWith(finalResult)
                    .doOnNext(result -> {
                        if (!result.isDone()) {
                            log.debug("Streaming text length: {}", result.text().length());
                        } else {
                            log.info("Stream completed with {} recognized stocks", 
                                result.barStocks() != null ? result.barStocks().size() : 0);
                        }
                    })
                    .doOnError(e -> log.error("Error processing image for stock recognition stream", e))
                    .onErrorReturn(new StreamBarStockResult(true, "Error occurred during processing", new ArrayList<>()));
                    
        } catch (Exception e) {
            log.error("Error setting up image recognition stream", e);
            return Flux.just(new StreamBarStockResult(true, "Error setting up stream: " + e.getMessage(),
                    new ArrayList<>()));
        }
    }

    public List<BarStock> recognizeStockFromImage(String imageId) {
        try {

            List<Message> messages = new ArrayList<>();
            messages.add(new SystemMessage(promptTemplate));
            Resource image = imageService.loadImage(imageId);
            String mimeType = contentTypeUtil.detectMimeType(image).toString();
            UserMessage userMessage = UserMessage.builder().text("这是原料图片，请开始识别").media(List.of(new Media(MimeType.valueOf(mimeType), image))).build();
            messages.add(userMessage);
            Prompt prompt = new Prompt(messages);
            ChatResponse call = chatModel.call(prompt);
            Generation result = call.getResult();
            String response = result.getOutput().getText();
            log.info("Image recognition response: {}", response);
            return parseRecognitionResponse(response);
        } catch (Exception e) {
            log.error("Error processing image for stock recognition", e);
            return new ArrayList<>();
        }
    }

    private List<BarStock> parseRecognitionResponse(String response) {
        try {
            // Extract JSON array from response if needed
            String jsonContent = extractJsonContent(response);

            BarStockCreateReq barStockCreateReq = objectMapper.readValue(jsonContent, BarStockCreateReq.class);

            // Parse the JSON array into a list of InnerBarStockCreateReq objects
            List<InnerBarStockCreateReq> stockItems = barStockCreateReq.getBarStocks() == null ? new ArrayList<>()
                    : barStockCreateReq.getBarStocks();

            // Convert to BarStock objects
            List<BarStock> barStocks = new ArrayList<>();
            for (InnerBarStockCreateReq item : stockItems) {
                BarStock stock = new BarStock();
                stock.setName(item.getName());
                stock.setType(item.getType());
                stock.setIconType(item.getIconType());
                stock.setDescription(item.getDescription());
                barStocks.add(stock);
            }

            return barStocks;
        } catch (JsonProcessingException e) {
            log.error("Error parsing recognition response", e);
            return new ArrayList<>();
        }
    }

    private String extractJsonContent(String response) {
        if (response == null || response.trim().isEmpty()) {
            return "{\"bar_stocks\":[]}";
        }
        
        String trimmedResponse = response.trim();
        
        // If response contains JSON object directly
        if (trimmedResponse.startsWith("{") && trimmedResponse.endsWith("}")) {
            return trimmedResponse;
        }
        
        // If response contains JSON array directly
        if (trimmedResponse.startsWith("[") && trimmedResponse.endsWith("]")) {
            return "{\"bar_stocks\":" + trimmedResponse + "}";
        }

        // If JSON is wrapped in code blocks
        if (response.contains("```json") && response.contains("```")) {
            int start = response.indexOf("```json") + 7;
            int end = response.indexOf("```", start);
            if (end > start) {
                String jsonContent = response.substring(start, end).trim();
                if (jsonContent.startsWith("[") && jsonContent.endsWith("]")) {
                    return "{\"bar_stocks\":" + jsonContent + "}";
                }
                return jsonContent;
            }
        }

        // If no JSON format is found, return empty object
        return "{\"bar_stocks\":[]}";
    }
}