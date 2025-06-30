package cool.drinkup.drinkup.wine.internal.controller;

import com.mzt.logapi.starter.annotation.LogRecord;
import cool.drinkup.drinkup.common.log.event.WineEvent;
import cool.drinkup.drinkup.shared.spi.CommonResp;
import cool.drinkup.drinkup.wine.internal.controller.req.UpdateCardImageRequest;
import cool.drinkup.drinkup.wine.internal.controller.resp.RandomWineResp;
import cool.drinkup.drinkup.wine.internal.controller.resp.WorkflowUserWineVo;
import cool.drinkup.drinkup.wine.internal.controller.resp.WorkflowWineVo;
import cool.drinkup.drinkup.wine.internal.mapper.UserWineMapper;
import cool.drinkup.drinkup.wine.internal.mapper.WineMapper;
import cool.drinkup.drinkup.wine.internal.model.UserWine;
import cool.drinkup.drinkup.wine.internal.model.Wine;
import cool.drinkup.drinkup.wine.internal.rag.DataLoaderService;
import cool.drinkup.drinkup.wine.internal.service.MixedWineService;
import cool.drinkup.drinkup.wine.internal.service.UserWineService;
import cool.drinkup.drinkup.wine.internal.service.WineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wines")
@RequiredArgsConstructor
@Tag(name = "酒管理", description = "提供酒相关的API接口")
public class WineController {

    private final WineService wineService;
    private final UserWineService userWineService;
    private final WineMapper wineMapper;
    private final UserWineMapper userWineMapper;
    private final DataLoaderService dataLoaderService;
    private final MixedWineService mixedWineService;

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询酒", description = "通过酒的唯一标识符查询详细信息")
    @Parameter(name = "id", description = "酒ID", required = true)
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "成功获取酒信息"),
                @ApiResponse(responseCode = "404", description = "未找到指定ID的酒")
            })
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommonResp<WorkflowWineVo>> getWineById(@PathVariable Long id) {
        Wine wineById = wineService.getWineById(id);
        if (wineById == null) {
            return ResponseEntity.notFound().build();
        }
        WorkflowWineVo wineVo = wineMapper.toWineVo(wineById);
        return ResponseEntity.ok(CommonResp.success(wineVo));
    }

    @GetMapping("/by-tag")
    @Operation(summary = "查询酒列表", description = "通过标签查询酒列表，支持分页。如果不提供标签则返回所有酒")
    @Parameter(name = "tag", description = "标签名称，可选参数", required = false)
    @Parameter(name = "page", description = "页码，从0开始", required = false)
    @Parameter(name = "size", description = "每页大小", required = false)
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "成功获取酒列表"),
                @ApiResponse(responseCode = "400", description = "请求参数错误")
            })
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommonResp<Page<WorkflowWineVo>>> getWinesByTag(
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String iba,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<Wine> wines = wineService.getWinesByTag(tag, iba, pageRequest);
        Page<WorkflowWineVo> wineVos = wines.map(wineMapper::toWineVo);
        return ResponseEntity.ok(CommonResp.success(wineVos));
    }

    @GetMapping("/user-wine")
    @Operation(summary = "查询用户酒列表", description = "查询当前用户的所有酒")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommonResp<Page<WorkflowUserWineVo>>> getUserWine(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("id").descending());
        Page<UserWine> userWine = userWineService.getUserWine(pageRequest);
        Page<WorkflowUserWineVo> userWineVos = userWine.map(userWineMapper::toWorkflowUserWineVo);
        return ResponseEntity.ok(CommonResp.success(userWineVos));
    }

    @Operation(summary = "加载酒类数据到向量数据库", description = "将酒类相关数据加载到系统中")
    @ApiResponse(responseCode = "200", description = "Successfully loaded wine data")
    @PostMapping("/vector-store/load-wine")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> loadWine() {
        dataLoaderService.loadData();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/vector-store/add-wine/{wineId}")
    @Operation(summary = "添加特定酒类数据到向量数据库", description = "将酒类数据添加到系统中")
    @Parameter(name = "wineId", description = "酒ID", required = true)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addWine(@PathVariable Long wineId) {
        dataLoaderService.addData(wineId);
        return ResponseEntity.ok().build();
    }

    @LogRecord(
            type = WineEvent.WINE,
            subType = WineEvent.BehaviorEvent.WINE_RANDOM,
            bizNo = "{{#type}}-{{#count}}",
            success = "用户随机获取{{#count}}杯{{#type}}类型的酒")
    @GetMapping("/random")
    @Operation(summary = "随机来一杯或多杯", description = "根据类型随机获取酒，支持三种类型：mixed(完全随机)、user(用户酒库随机)、iba(AI酒单随机)")
    @Parameter(name = "type", description = "随机类型：mixed(完全随机)、user(用户酒库随机)、iba(酒单随机)", required = true)
    @Parameter(name = "count", description = "数量，默认为1", required = false)
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "成功获取随机酒"),
                @ApiResponse(responseCode = "400", description = "请求参数错误"),
                @ApiResponse(responseCode = "404", description = "未找到符合条件的酒")
            })
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommonResp<RandomWineResp>> getRandomWine(
            @RequestParam String type, @RequestParam(defaultValue = "1") int count) {
        // 验证数量参数
        if (count <= 0 || count > 20) {
            return ResponseEntity.badRequest().body(CommonResp.error("数量参数必须在1-20之间"));
        }

        return ResponseEntity.ok(CommonResp.success(mixedWineService.getRandomWine(type, count)));
    }

    @PatchMapping("/user-wine/{id}")
    @Operation(summary = "更新用户酒单", description = "更新指定用户酒单的卡片图片")
    @Parameter(name = "id", description = "用户酒ID", required = true)
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "成功更新卡片图片"),
                @ApiResponse(responseCode = "400", description = "请求参数错误"),
                @ApiResponse(responseCode = "404", description = "未找到指定的用户酒"),
                @ApiResponse(responseCode = "403", description = "无权限修改此用户酒")
            })
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommonResp<WorkflowUserWineVo>> updateUserWineCardImage(
            @PathVariable Long id, @Valid @RequestBody UpdateCardImageRequest request) {
        try {
            UserWine updatedUserWine = userWineService.updateUserWineCardImage(id, request.getCardImage());
            WorkflowUserWineVo userWineVo = userWineMapper.toWorkflowUserWineVo(updatedUserWine);
            return ResponseEntity.ok(CommonResp.success(userWineVo));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(CommonResp.error(e.getMessage()));
        }
    }

    @PatchMapping("/{id}/card-image")
    @Operation(summary = "更新酒单", description = "更新指定酒的卡片图片")
    @Parameter(name = "id", description = "酒ID", required = true)
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "成功更新卡片图片"),
                @ApiResponse(responseCode = "400", description = "请求参数错误"),
                @ApiResponse(responseCode = "404", description = "未找到指定的酒")
            })
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommonResp<WorkflowWineVo>> updateWineCardImage(
            @PathVariable Long id, @Valid @RequestBody UpdateCardImageRequest request) {
        try {
            Wine updatedWine = wineService.updateWineCardImage(id, request.getCardImage());
            WorkflowWineVo wineVo = wineMapper.toWineVo(updatedWine);
            return ResponseEntity.ok(CommonResp.success(wineVo));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(CommonResp.error(e.getMessage()));
        }
    }
}
