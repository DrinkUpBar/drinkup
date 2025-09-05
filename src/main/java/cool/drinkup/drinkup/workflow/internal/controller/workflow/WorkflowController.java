package cool.drinkup.drinkup.workflow.internal.controller.workflow;

import com.mzt.logapi.starter.annotation.LogRecord;
import cool.drinkup.drinkup.common.log.event.AIChatEvent;
import cool.drinkup.drinkup.common.log.event.WineEvent;
import cool.drinkup.drinkup.shared.dto.WorkflowBartenderChatDto;
import cool.drinkup.drinkup.shared.spi.CommonResp;
import cool.drinkup.drinkup.user.spi.AuthenticatedUserDTO;
import cool.drinkup.drinkup.user.spi.AuthenticationServiceFacade;
import cool.drinkup.drinkup.wine.spi.WorkflowWineResp;
import cool.drinkup.drinkup.workflow.internal.controller.workflow.req.WorkflowBartenderChatReq;
import cool.drinkup.drinkup.workflow.internal.controller.workflow.req.WorkflowBartenderChatV2Req;
import cool.drinkup.drinkup.workflow.internal.controller.workflow.req.WorkflowBartenderUserDemandReq;
import cool.drinkup.drinkup.workflow.internal.controller.workflow.req.WorkflowMaterialAnalysisReq;
import cool.drinkup.drinkup.workflow.internal.controller.workflow.req.WorkflowSaveMemoryReq;
import cool.drinkup.drinkup.workflow.internal.controller.workflow.req.WorkflowStockRecognitionReq;
import cool.drinkup.drinkup.workflow.internal.controller.workflow.req.WorkflowStockRecognitionStreamReq;
import cool.drinkup.drinkup.workflow.internal.controller.workflow.req.WorkflowTranslateReq;
import cool.drinkup.drinkup.workflow.internal.controller.workflow.req.WorkflowUserChatReq;
import cool.drinkup.drinkup.workflow.internal.controller.workflow.req.WorkflowUserChatV2Req;
import cool.drinkup.drinkup.workflow.internal.controller.workflow.req.WorkflowUserChatV2StreamReq;
import cool.drinkup.drinkup.workflow.internal.controller.workflow.req.WorkflowUserReq;
import cool.drinkup.drinkup.workflow.internal.controller.workflow.resp.WorkflowMaterialAnalysisResp;
import cool.drinkup.drinkup.workflow.internal.controller.workflow.resp.WorkflowSaveMemoryResp;
import cool.drinkup.drinkup.workflow.internal.controller.workflow.resp.WorkflowStockRecognitionResp;
import cool.drinkup.drinkup.workflow.internal.controller.workflow.resp.WorkflowStockRecognitionStreamResp;
import cool.drinkup.drinkup.workflow.internal.controller.workflow.resp.WorkflowTranslateResp;
import cool.drinkup.drinkup.workflow.internal.controller.workflow.resp.WorkflowUserChatResp;
import cool.drinkup.drinkup.workflow.internal.controller.workflow.resp.WorkflowUserChatV2Resp;
import cool.drinkup.drinkup.workflow.internal.controller.workflow.resp.WorkflowUserChatV2StreamResp;
import cool.drinkup.drinkup.workflow.internal.service.WorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequestMapping("/api/workflow")
@RequiredArgsConstructor
@Tag(name = "调酒工作流", description = "调酒工作流相关操作API")
public class WorkflowController {

    private final WorkflowService workflowService;
    private final AuthenticationServiceFacade authenticationServiceFacade;

    @LogRecord(
            type = WineEvent.WINE,
            subType = WineEvent.BehaviorEvent.COCKTAIL_REQUEST,
            bizNo = "null",
            success = "用户调酒单请求成功, 请求内容：{{#userInput.userInput}}")
    @Operation(summary = "处理调酒单请求", description = "处理用户输入的鸡尾酒相关工作流")
    @ApiResponse(responseCode = "200", description = "Successfully processed cocktail request")
    @PostMapping("/cocktail")
    public ResponseEntity<CommonResp<WorkflowWineResp>> processCocktailRequest(
            @Parameter(description = "User input for cocktail workflow") @RequestBody WorkflowUserReq userInput) {
        var resp = workflowService.processCocktailRequest(userInput);
        return ResponseEntity.ok(CommonResp.success(resp));
    }

    @LogRecord(
            type = AIChatEvent.AI_CHAT,
            subType = AIChatEvent.BehaviorEvent.AI_CHAT,
            bizNo = "null",
            success = "用户AI聊天成功, 用户请求：{{#userInput.messages.$[#this != null].content}}",
            extra = "{{@logExtraUtil.getLogExtra(#_ret.body.data)}}")
    @Operation(summary = "与机器人聊天", description = "与机器人进行对话", deprecated = true)
    @ApiResponse(responseCode = "200", description = "Successfully chatted with the bot")
    @PostMapping("/chat")
    @PreAuthorize("isAuthenticated()")
    @Deprecated
    public ResponseEntity<CommonResp<WorkflowUserChatResp>> chat(@RequestBody WorkflowUserChatReq userInput) {
        var resp = workflowService.chat(userInput);
        if (resp == null) {
            return ResponseEntity.ok(CommonResp.error("Error chatting with the bot"));
        }
        return ResponseEntity.ok(CommonResp.success(resp));
    }

    @LogRecord(
            type = AIChatEvent.AI_CHAT,
            subType = AIChatEvent.BehaviorEvent.AI_CHAT,
            bizNo = "null",
            success = "用户AI聊天成功, 用户请求：{{#userInput.userMessage}}," + " 对话请求Id:{{#_ret.body.data.conversationId}}",
            extra = "{{@logExtraUtil.getLogExtra(#_ret.body.data)}}")
    @Operation(summary = "与机器人聊天v2", description = "与机器人进行对话v2")
    @ApiResponse(responseCode = "200", description = "Successfully chatted with the bot")
    @PostMapping("/v2/chat")
    @PreAuthorize("isAuthenticated()")
    @Deprecated
    public ResponseEntity<CommonResp<WorkflowUserChatV2Resp>> chatV2(@RequestBody WorkflowUserChatV2Req userInput) {
        var resp = workflowService.chatV2(userInput);
        if (resp == null) {
            return ResponseEntity.ok(CommonResp.error("Error chatting with the bot"));
        }
        return ResponseEntity.ok(CommonResp.success(resp));
    }

    @LogRecord(
            type = AIChatEvent.AI_CHAT,
            subType = AIChatEvent.BehaviorEvent.BARTENDER_CHAT,
            bizNo = "{{#_ret.body.data.id}}",
            success = "用户调酒师聊天成功，生成酒单：{{#_ret.body.data.name}}",
            extra = "{{@logExtraUtil.getLogExtra(#bartenderInput)}}")
    @Operation(summary = "与调酒师聊天", description = "与调酒师进行对话", deprecated = true)
    @ApiResponse(responseCode = "200", description = "Successfully chatted with the bartender")
    @PostMapping("/bartender")
    @PreAuthorize("isAuthenticated()")
    @Deprecated
    public ResponseEntity<CommonResp<WorkflowBartenderChatDto>> mixDrink(
            @RequestBody WorkflowBartenderChatReq bartenderInput) {
        var resp = workflowService.mixDrink(bartenderInput);
        if (resp == null) {
            return ResponseEntity.ok(CommonResp.error("Error mixing drink"));
        }
        return ResponseEntity.ok(CommonResp.success(resp));
    }

    @LogRecord(
            type = AIChatEvent.AI_CHAT,
            subType = AIChatEvent.BehaviorEvent.BARTENDER_CHAT,
            bizNo = "{{#_ret.body.data.id}}",
            success = "用户调酒师聊天成功，生成酒单：{{#_ret.body.data.name}}",
            extra = "{{@logExtraUtil.getLogExtra(#bartenderInput)}}")
    @Operation(summary = "与调酒师聊天v2", description = "与调酒师进行对话")
    @ApiResponse(responseCode = "200", description = "Successfully chatted with the bartender")
    @PostMapping("/v2/bartender")
    @PreAuthorize("isAuthenticated()")
    @Deprecated
    public ResponseEntity<CommonResp<WorkflowBartenderChatDto>> mixDrinkV2(
            @RequestBody WorkflowBartenderChatV2Req bartenderInput) {
        var resp = workflowService.mixDrinkV2(bartenderInput);
        if (resp == null) {
            return ResponseEntity.ok(CommonResp.error("Error mixing drink"));
        }
        return ResponseEntity.ok(CommonResp.success(resp));
    }

    @LogRecord(
            type = AIChatEvent.AI_CHAT,
            subType = AIChatEvent.BehaviorEvent.BARTENDER_CHAT,
            bizNo = "{{#_ret.body.data.id}}",
            success = "用户调酒师聊天成功(公开)，生成酒单：{{#_ret.body.data.name}}",
            extra = "{{@logExtraUtil.getLogExtra(#bartenderInput)}}")
    @Operation(summary = "与调酒师聊天v2(公开)", description = "与调酒师进行对话（公开接口，仅userDemand）")
    @ApiResponse(responseCode = "200", description = "Successfully chatted with the bartender (public)")
    @PostMapping("/v2/bartender/public")
    public ResponseEntity<CommonResp<WorkflowBartenderChatDto>> mixDrinkV2Public(
            @RequestBody WorkflowBartenderUserDemandReq bartenderInput) {
        var v2Req = new WorkflowBartenderChatV2Req();
        v2Req.setUserDemand(bartenderInput.getUserDemand());
        var resp = workflowService.mixDrinkV2(v2Req, bartenderInput.getUserId());
        if (resp == null) {
            return ResponseEntity.ok(CommonResp.error("Error mixing drink"));
        }
        return ResponseEntity.ok(CommonResp.success(resp));
    }

    @LogRecord(
            type = AIChatEvent.AI_CHAT,
            subType = AIChatEvent.BehaviorEvent.STOCK_RECOGNITION,
            bizNo = "{{#barId}}",
            success = "用户库存识别成功，识别到{{#_ret.body.data.recognizedStocks.size()}}种库存")
    @Operation(summary = "库存识别", description = "通过图片识别库存")
    @ApiResponse(responseCode = "200", description = "Successfully recognized stock from image")
    @PostMapping(value = "/recognize-stock/{barId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommonResp<WorkflowStockRecognitionResp>> recognizeStock(
            @Parameter(description = "Bar ID") @PathVariable("barId") Long barId,
            @Parameter(description = "Image file for stock recognition") @RequestBody WorkflowStockRecognitionReq req) {
        req.setBarId(barId);
        var resp = workflowService.recognizeStock(req);
        if (resp == null) {
            return ResponseEntity.ok(CommonResp.error("Error recognizing stock"));
        }
        return ResponseEntity.ok(CommonResp.success(resp));
    }

    @LogRecord(
            type = AIChatEvent.AI_CHAT,
            subType = AIChatEvent.BehaviorEvent.STOCK_RECOGNITION,
            bizNo = "{{#barId}}",
            success = "用户库存识别成功通过流式库存识别")
    @Operation(summary = "流式库存识别", description = "通过图片流式识别库存")
    @ApiResponse(responseCode = "200", description = "Successfully recognized stock from image")
    @PostMapping(
            value = "/recognize-stock-stream",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_NDJSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public Flux<WorkflowStockRecognitionStreamResp> recognizeStockStream(
            @Parameter(description = "Image file for stock recognition") @RequestBody
                    WorkflowStockRecognitionStreamReq req) {
        return workflowService.recognizeStockStream(req);
    }

    @LogRecord(
            type = AIChatEvent.AI_CHAT,
            subType = AIChatEvent.BehaviorEvent.AI_TRANSLATE,
            bizNo = "null",
            success = "AI翻译成功，翻译内容：{{#translateReq.text}}",
            extra = "{{@logExtraUtil.getLogExtra(#translateReq)}}")
    @Operation(summary = "AI翻译", description = "使用AI进行文本翻译")
    @ApiResponse(responseCode = "200", description = "Successfully translated text")
    @PostMapping("/translate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommonResp<WorkflowTranslateResp>> translate(
            @Parameter(description = "Translation request") @RequestBody WorkflowTranslateReq translateReq) {
        var resp = workflowService.translate(translateReq);
        if (resp == null) {
            return ResponseEntity.ok(CommonResp.error("AI翻译失败"));
        }
        return ResponseEntity.ok(CommonResp.success(resp));
    }

    @LogRecord(
            type = AIChatEvent.AI_CHAT,
            subType = AIChatEvent.MaterialEvent.MATERIAL_ANALYSIS,
            bizNo = "null",
            success = "AI材料解读成功，解读材料",
            extra = "{{@logExtraUtil.getLogExtra(#materialReq)}}")
    @Operation(summary = "AI材料解读", description = "使用AI解读酒类材料信息")
    @ApiResponse(responseCode = "200", description = "Successfully analyzed material")
    @PostMapping("/analyze-material")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommonResp<WorkflowMaterialAnalysisResp>> analyzeMaterial(
            @Parameter(description = "Material analysis request") @RequestBody
                    WorkflowMaterialAnalysisReq materialReq) {
        var resp = workflowService.analyzeMaterial(materialReq);
        if (resp == null) {
            return ResponseEntity.ok(CommonResp.error("AI材料解读失败"));
        }
        return ResponseEntity.ok(CommonResp.success(resp));
    }

    @LogRecord(
            type = AIChatEvent.AI_CHAT,
            subType = AIChatEvent.BehaviorEvent.AI_CHAT,
            bizNo = "null",
            success = "用户AI流式聊天成功, 用户请求：{{#userInput.userMessage}}")
    @Operation(summary = "与机器人流式聊天v2", description = "与机器人进行流式对话v2，透传到Python Agent，返回所有中间事件")
    @ApiResponse(responseCode = "200", description = "Successfully started streaming chat with the bot")
    @PostMapping(value = "/v2/chat/stream", produces = MediaType.APPLICATION_NDJSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public Flux<CommonResp<WorkflowUserChatV2StreamResp>> chatV2Stream(
            @RequestBody WorkflowUserChatV2StreamReq userInput) {
        // 从认证信息中获取用户ID
        String userId = authenticationServiceFacade
                .getCurrentAuthenticatedUser()
                .map(AuthenticatedUserDTO::userId)
                .map(String::valueOf)
                .orElse("anonymous");

        log.info("Starting streaming chat v2 for user: {}, message: {}", userId, userInput.getUserMessage());

        return workflowService
                .chatV2Stream(userInput, userId)
                .map(resp -> {
                    if (resp != null) {
                        return CommonResp.success(resp);
                    } else {
                        return CommonResp.<WorkflowUserChatV2StreamResp>error("Error in streaming chat with the bot");
                    }
                })
                .onErrorReturn(CommonResp.<WorkflowUserChatV2StreamResp>error("Error in streaming chat with the bot"));
    }

    @LogRecord(
            type = AIChatEvent.AI_CHAT,
            subType = AIChatEvent.BehaviorEvent.SAVE_MEMORY,
            bizNo = "{{#req.conversationId}}",
            success = "保存会话记忆成功, 对话ID: {{#req.conversationId}}")
    @Operation(summary = "保存会话记忆", description = "将会话内容保存到用户记忆库")
    @ApiResponse(responseCode = "200", description = "Successfully saved conversation to memory")
    @PostMapping("/conversation/save-memory")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommonResp<WorkflowSaveMemoryResp>> saveConversationMemory(
            @Parameter(description = "Save memory request") @RequestBody WorkflowSaveMemoryReq req) {
        // 从认证信息中获取用户ID
        String userId = authenticationServiceFacade
                .getCurrentAuthenticatedUser()
                .map(AuthenticatedUserDTO::userId)
                .map(String::valueOf)
                .orElse("anonymous");

        log.info("Saving conversation memory for user: {}, conversationId: {}", userId, req.getConversationId());

        // 异步保存记忆，立即返回
        workflowService.saveConversationMemoryAsync(req.getConversationId(), userId);

        // 立即返回成功响应
        WorkflowSaveMemoryResp resp = new WorkflowSaveMemoryResp();
        resp.setStatus("accepted");
        resp.setMessagesSaved(0); // 表示已接受请求，实际数量在异步处理中确定
        return ResponseEntity.ok(CommonResp.success(resp));
    }
}
