package cool.drinkup.drinkup.workflow.internal.service;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import cool.drinkup.drinkup.shared.dto.UserWine;
import cool.drinkup.drinkup.shared.dto.WorkflowBartenderChatDto;
import cool.drinkup.drinkup.wine.spi.UserWineServiceFacade;
import cool.drinkup.drinkup.wine.spi.WineServiceFacade;
import cool.drinkup.drinkup.wine.spi.WorkflowWineResp;
import cool.drinkup.drinkup.workflow.internal.controller.req.WorkflowBartenderChatReq;
import cool.drinkup.drinkup.workflow.internal.controller.req.WorkflowMaterialAnalysisReq;
import cool.drinkup.drinkup.workflow.internal.controller.req.WorkflowStockRecognitionReq;
import cool.drinkup.drinkup.workflow.internal.controller.req.WorkflowTranslateReq;
import cool.drinkup.drinkup.workflow.internal.controller.req.WorkflowUserChatReq;
import cool.drinkup.drinkup.workflow.internal.controller.req.WorkflowUserReq;
import cool.drinkup.drinkup.workflow.internal.controller.resp.WorkflowMaterialAnalysisResp;
import cool.drinkup.drinkup.workflow.internal.controller.resp.WorkflowStockRecognitionResp;
import cool.drinkup.drinkup.workflow.internal.controller.resp.WorkflowStockRecognitionStreamResp;
import cool.drinkup.drinkup.workflow.internal.controller.resp.WorkflowTranslateResp;
import cool.drinkup.drinkup.workflow.internal.controller.resp.WorkflowUserChatResp;
import cool.drinkup.drinkup.workflow.internal.model.Bar;
import cool.drinkup.drinkup.workflow.internal.model.BarStock;
import cool.drinkup.drinkup.workflow.internal.service.bar.BarService;
import cool.drinkup.drinkup.workflow.internal.service.bartender.BartenderService;
import cool.drinkup.drinkup.workflow.internal.service.bartender.dto.BartenderParams;
import cool.drinkup.drinkup.workflow.internal.service.bartender.theme.Theme;
import cool.drinkup.drinkup.workflow.internal.service.bartender.theme.ThemeEnum;
import cool.drinkup.drinkup.workflow.internal.service.bartender.theme.ThemeFactory;
import cool.drinkup.drinkup.workflow.internal.service.chat.ChatBotService;
import cool.drinkup.drinkup.workflow.internal.service.chat.dto.ChatParams;
import cool.drinkup.drinkup.workflow.internal.service.image.ImageGenerateService;
import cool.drinkup.drinkup.workflow.internal.service.image.ImageRecognitionService;
import cool.drinkup.drinkup.workflow.internal.service.image.ImageService;
import cool.drinkup.drinkup.workflow.internal.service.material.MaterialAnalysisService;
import cool.drinkup.drinkup.workflow.internal.service.material.MaterialAnalysisService.MaterialAnalysisResult;
import cool.drinkup.drinkup.workflow.internal.service.stock.BarStockService;
import cool.drinkup.drinkup.workflow.internal.service.translate.TranslateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowService {

    private final WineServiceFacade wineServiceFacade;
    private final UserWineServiceFacade userWineServiceFacade;
    private final ChatBotService chatBotService;
    private final BartenderService bartenderService;
    private final BarStockService barStockService;
    private final BarService barService;
    private final ObjectMapper objectMapper;
    private final ImageRecognitionService imageRecognitionService;
    private final ImageService imageService;
    private final ThemeFactory themeFactory;
    private final ImageGenerateService imageGenerateService;
    private final TranslateService translateService;
    private final MaterialAnalysisService materialAnalysisService;

    public WorkflowWineResp processCocktailRequest(WorkflowUserReq userInput) {
        String userInputText = userInput.getUserInput();
        return wineServiceFacade.processCocktailRequest(userInputText);
    }

    public WorkflowUserChatResp chat(WorkflowUserChatReq userInput) {
        List<Bar> bars = barService.getUserBarByBarIds(userInput.getBarIds());
        ChatParams chatParams = buildChatParams(bars, userInput.getImageId());
        var chatWithUser = chatBotService.chat(userInput.getMessages(), chatParams);
        var json = extractJson(chatWithUser);
        try {
            var chatBotResponse = objectMapper.readValue(json, WorkflowUserChatResp.class);
            return chatBotResponse;
        } catch (JsonProcessingException e) {
            log.error("Error parsing JSON: {}", e.getMessage());
            return null;
        }
    }

    private String extractJson(String chatWithUser) {
        if ( !chatWithUser.contains("```json")) {
            return chatWithUser;
        }
        // Extract JSON content between ```json and ``` markers
        String jsonPattern = "```json\s*(.*?)\s*```";
        Pattern pattern = Pattern.compile(jsonPattern, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(chatWithUser);

        if ( matcher.find()) {
            return matcher.group(1).trim();
        }

        // If no JSON found in the expected format, return the original string
        // This handles cases where the response might not be properly formatted
        return chatWithUser;
    }

    public WorkflowBartenderChatDto mixDrink(WorkflowBartenderChatReq bartenderInput) {
        var bartenderParam = buildBartenderParams(bartenderInput);
        var chatWithBartender = bartenderService.generateDrink(bartenderInput.getMessages(), bartenderParam);
        var json = extractJson(chatWithBartender);
        try {
            var chatBotResponse = objectMapper.readValue(json, WorkflowBartenderChatDto.class);
            String imageUrl = imageGenerateService.generateImage(chatBotResponse.getImagePrompt());
            String imageId = imageService.storeImage(imageUrl);
            chatBotResponse.setImage(imageId);
            // Convert workflow response to wine response for saving
            UserWine saveUserWine = userWineServiceFacade.saveUserWine(chatBotResponse);
            chatBotResponse.setId(saveUserWine.getId());
            chatBotResponse.setImage(imageService.getImageUrl(imageId));
            return chatBotResponse;
        } catch (JsonProcessingException e) {
            log.error("Error parsing JSON: {}", e.getMessage());
            return null;
        }
    }

    private BartenderParams buildBartenderParams(WorkflowBartenderChatReq bartenderInput) {
        List<Bar> userBars = barService.getUserBarByBarIds(bartenderInput.getBarIds());
        String userStock = buildBarDescription(userBars);
        Theme theme = themeFactory.getTheme(ThemeEnum.fromValue(bartenderInput.getTheme()));
        return BartenderParams.builder()
                .userStock(userStock)
                .userDemand(bartenderInput.getUserDemand())
                .theme(theme.getName())
                .build();
    }

    private ChatParams buildChatParams(List<Bar> bars, String imageId) {
        ChatParams chatParams = new ChatParams();
        chatParams.setUserStock(buildBarDescription(bars));
        chatParams.setImageId(StringUtils.hasText(imageId) ? imageId : null);
        return chatParams;
    }

    private String buildBarDescription(List<Bar> bars) {
        if ( CollectionUtils.isEmpty(bars)) {
            return "null";
        }
        return bars.stream()
                .map(Bar::getBarDescription)
                .collect(Collectors.joining("\n"));
    }

    public WorkflowStockRecognitionResp recognizeStock(WorkflowStockRecognitionReq req) {
        try {
            // 使用图像识别服务识别库存
            List<BarStock> recognizedStocks = imageRecognitionService.recognizeStockFromImage(req.getImageId());

            Bar bar = new Bar();
            bar.setId(req.getBarId());
            // 设置barId
            recognizedStocks.forEach(stock -> stock.setBar(bar));

            // 保存识别的库存到数据库
            List<BarStock> savedStocks = barStockService.saveAll(recognizedStocks);

            // 构建响应
            WorkflowStockRecognitionResp resp = new WorkflowStockRecognitionResp();
            resp.setBarId(req.getBarId());
            resp.setRecognizedStocks(savedStocks);

            return resp;
        } catch (Exception e) {
            log.error("Error recognizing stock from image", e);
            return null;
        }
    }

    public Flux<WorkflowStockRecognitionStreamResp> recognizeStockStream(WorkflowStockRecognitionReq req) {
        return imageRecognitionService.recognizeStockFromImageStream(req.getImageId())
                .map(result -> {
                    WorkflowStockRecognitionStreamResp resp = new WorkflowStockRecognitionStreamResp();
                    resp.setDone(result.isDone());
                    resp.setText(result.text());
                    resp.setBarId(req.getBarId());
                    resp.setRecognizedStocks(result.barStocks());
                    return resp;
                }).doOnNext(resp -> {
                    if ( resp.isDone() && resp.getRecognizedStocks() != null && !resp.getRecognizedStocks().isEmpty()) {
                        try {
                            Bar bar = new Bar();
                            bar.setId(req.getBarId());
                            resp.getRecognizedStocks().forEach(stock -> stock.setBar(bar));
                            List<BarStock> savedStocks = barStockService.saveAll(resp.getRecognizedStocks());
                            resp.setRecognizedStocks(savedStocks);
                        } catch (Exception e) {
                            log.error("Error saving recognized stocks for barId: {}", req.getBarId(), e);
                        }
                    }
                })
                .doOnError(
                        error -> log.error("Error in stock recognition stream for barId: {}", req.getBarId(), error));
    }

    /**
     * AI翻译
     */
    public WorkflowTranslateResp translate(WorkflowTranslateReq req) {
        try {
            long startTime = System.currentTimeMillis();

            // 调用AI翻译服务
            String translatedText = translateService.translate(
                    req.getText(),
                    req.getTargetLanguage(),
                    req.getScene());

            WorkflowTranslateResp resp = new WorkflowTranslateResp();
            resp.setTranslatedText(translatedText);

            return resp;
        } catch (Exception e) {
            log.error("AI翻译失败", e);
            return null;
        }
    }

    public WorkflowMaterialAnalysisResp analyzeMaterial(WorkflowMaterialAnalysisReq materialReq) {
        MaterialAnalysisResult result = materialAnalysisService.analyzeMaterial(materialReq.getMaterialId());
        if (result == null) {
            return null;
        }
        WorkflowMaterialAnalysisResp resp = new WorkflowMaterialAnalysisResp();
        resp.setDescription(result.description());
        return resp;
    }
}
