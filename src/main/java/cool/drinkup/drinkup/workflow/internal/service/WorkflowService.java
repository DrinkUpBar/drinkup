 
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
import cool.drinkup.drinkup.workflow.internal.constant.WorkflowConstant;
import cool.drinkup.drinkup.workflow.internal.controller.workflow.req.WorkflowBartenderChatReq;
import cool.drinkup.drinkup.workflow.internal.controller.workflow.req.WorkflowBartenderChatV2Req;
import cool.drinkup.drinkup.workflow.internal.controller.workflow.req.WorkflowMaterialAnalysisReq;
import cool.drinkup.drinkup.workflow.internal.controller.workflow.req.WorkflowStockRecognitionReq;
import cool.drinkup.drinkup.workflow.internal.controller.workflow.req.WorkflowStockRecognitionStreamReq;
import cool.drinkup.drinkup.workflow.internal.controller.workflow.req.WorkflowTranslateReq;
import cool.drinkup.drinkup.workflow.internal.controller.workflow.req.WorkflowUserChatReq;
import cool.drinkup.drinkup.workflow.internal.controller.workflow.req.WorkflowUserChatV2Req;
import cool.drinkup.drinkup.workflow.internal.controller.workflow.req.WorkflowUserReq;
import cool.drinkup.drinkup.workflow.internal.controller.workflow.req.info.Attachment;
import cool.drinkup.drinkup.workflow.internal.controller.workflow.req.info.BarAttachment;
import cool.drinkup.drinkup.workflow.internal.controller.workflow.req.info.ImageAttachment;
import cool.drinkup.drinkup.workflow.internal.controller.workflow.req.info.MaterialAttachment;
import cool.drinkup.drinkup.workflow.internal.controller.workflow.resp.WorkflowMaterialAnalysisResp;
import cool.drinkup.drinkup.workflow.internal.controller.workflow.resp.WorkflowStockRecognitionResp;
import cool.drinkup.drinkup.workflow.internal.controller.workflow.resp.WorkflowStockRecognitionStreamResp;
import cool.drinkup.drinkup.workflow.internal.controller.workflow.resp.WorkflowTranslateResp;
import cool.drinkup.drinkup.workflow.internal.controller.workflow.resp.WorkflowUserChatResp;
import cool.drinkup.drinkup.workflow.internal.controller.workflow.resp.WorkflowUserChatV2Resp;
import cool.drinkup.drinkup.workflow.internal.model.Bar;
import cool.drinkup.drinkup.workflow.internal.model.BarStock;
import cool.drinkup.drinkup.workflow.internal.model.Material;
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
import cool.drinkup.drinkup.workflow.internal.service.material.MaterialService;
import cool.drinkup.drinkup.workflow.internal.service.stock.BarStockService;
import cool.drinkup.drinkup.workflow.internal.service.translate.TranslateService;
import cool.drinkup.drinkup.workflow.internal.util.StockDescriptionUtil;
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
    private final MaterialService materialService;
    private final StockDescriptionUtil stockDescriptionUtil;

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
            log.error("Error parsing JSON: {}", e.getMessage(), e);
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

    @Deprecated
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

    private BartenderParams buildBartenderParams(WorkflowBartenderChatV2Req bartenderInput) {
            Theme theme = themeFactory.getTheme(ThemeEnum.fromValue(bartenderInput.getTheme()));
            return BartenderParams.builder()
                    .userStock(buildStockDescription(bartenderInput.getAttachment()))
                    .userDemand(bartenderInput.getUserDemand())
                    .theme(theme.getName())
                .build();
    }

    public WorkflowBartenderChatDto mixDrinkV2(WorkflowBartenderChatV2Req bartenderInput) {
        var bartenderParam = buildBartenderParams(bartenderInput);
        var chatWithBartender = bartenderService.generateDrinkV2(bartenderInput.getConversationId(), bartenderParam);
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

    private ChatParams buildChatParams(List<Bar> bars, String imageId) {
        ChatParams chatParams = new ChatParams();
        chatParams.setUserStock(buildBarDescription(bars));
        chatParams.setImageId(StringUtils.hasText(imageId) ? imageId : null);
        return chatParams;
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

    public Flux<WorkflowStockRecognitionStreamResp> recognizeStockStream(WorkflowStockRecognitionStreamReq req) {
        return imageRecognitionService.recognizeStockFromImageStream(req.getImageId())
                .map(result -> {
                    WorkflowStockRecognitionStreamResp resp = new WorkflowStockRecognitionStreamResp();
                    resp.setDone(result.isDone());
                    resp.setText(result.text());
                    resp.setRecognizedStocks(result.barStocks());
                    return resp;
                });
    }

    /**
     * AI翻译
     */
    public WorkflowTranslateResp translate(WorkflowTranslateReq req) {
        try {

            // 调用AI翻译服务
            String translatedText = translateService.translate(
                    req.getText());

            WorkflowTranslateResp resp = new WorkflowTranslateResp();
            resp.setTranslatedText(translatedText.trim());

            return resp;
        } catch (Exception e) {
            log.error("AI翻译失败", e);
            return null;
        }
    }

    public WorkflowMaterialAnalysisResp analyzeMaterial(WorkflowMaterialAnalysisReq materialReq) {
        String materialText = getMaterialText(materialReq);
        MaterialAnalysisResult result = materialAnalysisService.analyzeMaterial(materialText);
        if ( result == null) {
            return null;
        }
        WorkflowMaterialAnalysisResp resp = new WorkflowMaterialAnalysisResp();
        resp.setDescription(result.description());
        return resp;
    }

    private String getMaterialText(WorkflowMaterialAnalysisReq materialReq) {
        if (materialReq.getItem() == null) {
            return materialReq.getText();
        }
        if ( materialReq.getItem().getType().equalsIgnoreCase(WorkflowConstant.MATERIAL_TAG)) {
            Material material = materialService.getMaterialById(materialReq.getItem().getId());
            return material.getName();
        }
        if ( materialReq.getItem().getType().equalsIgnoreCase(WorkflowConstant.BAR_STOCK_TAG)) {
            BarStock barStock = barStockService.getBarStockById(materialReq.getItem().getId());
            return barStock.getName();
        }
        throw new RuntimeException("Invalid material analysis type: " + materialReq);
    }

    public WorkflowUserChatV2Resp chatV2(WorkflowUserChatV2Req userInput) {
        ChatBotService.ChatBotResponse chatResponse = chatBotService.chatV2(userInput.getConversationId(),
                userInput.getUserMessage(), buildChatParams(userInput));
        var json = extractJson(chatResponse.content());
        try {
            var chatBotResponse = objectMapper.readValue(json, WorkflowUserChatV2Resp.class);
            chatBotResponse.setConversationId(chatResponse.conversationId());
            return chatBotResponse;
        } catch (JsonProcessingException e) {
            log.error("Error parsing JSON: {}", e.getMessage(), e);
            return null;
        }

    }

    private ChatParams buildChatParams(WorkflowUserChatV2Req userInput) {
        List<ImageAttachment> imageAttachmentList = userInput.getAttachment()
                .getImageAttachmentList();
        List<ChatParams.ImageAttachment> imageAttachments = imageAttachmentList.stream().map(
                imageAttachment -> ChatParams.ImageAttachment.builder().imageId(
                        imageAttachment.getImageId()).build())
                .toList();
        return ChatParams.builder()
                .userStock(buildStockDescription(userInput.getAttachment()))
                .imageId(!imageAttachmentList.isEmpty() ? imageAttachmentList.getFirst().getImageId() : null)
                .imageAttachmentList(imageAttachments)
                .build();
    }

    private String buildStockDescription(Attachment attachment) {
        StringBuilder stockDescription = new StringBuilder();
        stockDescription.append("用户选取的所有材料如下：\n");
        List<BarAttachment> barAttachmentList = attachment.getBarAttachmentList();
        if ( barAttachmentList != null && !barAttachmentList.isEmpty()) {
            stockDescription.append("用户选取的库存材料如下：\n");
            for ( BarAttachment barAttachment : barAttachmentList) {
                stockDescription
                        .append(stockDescriptionUtil.getBarStockDescription(barAttachment.getBarId(),
                                barAttachment.getSelectedStockIdList()))
                        .append("\n");
            }
        }
        List<MaterialAttachment> materialAttachmentList = attachment.getMaterialAttachmentList();
        if ( materialAttachmentList != null && !materialAttachmentList.isEmpty()) {
            stockDescription.append("用户选取的预设材料如下：\n");
            for ( MaterialAttachment materialAttachment : materialAttachmentList) {
                stockDescription.append(stockDescriptionUtil.getMaterialStockDescription(
                        materialAttachment.getCategoryId(), materialAttachment.getSelectedMaterialIdList()))
                        .append("\n");
            }
        }
        return stockDescription.toString();
    }

}
