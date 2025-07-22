package cool.drinkup.drinkup.wine.internal.controller;

import cool.drinkup.drinkup.shared.spi.CommonResp;
import cool.drinkup.drinkup.wine.internal.service.refresh.AsyncImageProcessingService;
import cool.drinkup.drinkup.wine.internal.service.refresh.ImageProcessingTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "管理员图片处理", description = "管理员管理图片处理任务的接口，使用Redis队列和虚拟线程")
@SecurityRequirement(name = "bearerAuth")
public class AdminProcessingController {

    private final ImageProcessingTaskService taskService;
    private final AsyncImageProcessingService asyncService;

    /**
     * Create image processing tasks for unprocessed images
     */
    @PostMapping("/images/refresh")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "创建图片处理任务", description = "扫描用户-酒品和酒品表中未处理的图片，创建处理任务到Redis队列中")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "任务创建成功",
                content = @Content(schema = @Schema(implementation = Map.class))),
        @ApiResponse(
                responseCode = "400",
                description = "创建任务时出错",
                content = @Content(schema = @Schema(implementation = Map.class)))
    })
    public ResponseEntity<CommonResp<Map<String, Object>>> createImageProcessingTasks(
            @Parameter(description = "最大任务创建数量", example = "100") @RequestParam(defaultValue = "100") int batchSize) {

        log.info("Admin request to create image processing tasks, batch size: {}", batchSize);

        try {
            int createdTasks = taskService.createImageProcessingTasks(batchSize);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("createdTasks", createdTasks);
            response.put("message", String.format("Successfully created %d image processing tasks", createdTasks));

            return ResponseEntity.ok(CommonResp.success(response));

        } catch (Exception e) {
            log.error("Error creating image processing tasks", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(CommonResp.error(e.getMessage()));
        }
    }

    @Operation(summary = "获取图片处理任务状态", description = "获取图片处理任务的当前状态")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "成功获取任务状态",
                        content = @Content(schema = @Schema(implementation = CommonResp.class))),
                @ApiResponse(
                        responseCode = "400",
                        description = "请求参数无效",
                        content = @Content(schema = @Schema(implementation = CommonResp.class)))
            })
    @GetMapping("/images/status")
    public ResponseEntity<CommonResp<Map<String, Object>>> getImageProcessingStatus() {
        try {
            Map<String, Object> status = asyncService.getImageProcessingStatus();
            return ResponseEntity.ok(CommonResp.success(status));
        } catch (Exception e) {
            log.error("Error getting image processing status", e);
            return ResponseEntity.badRequest().body(CommonResp.error(e.getMessage()));
        }
    }

    @PostMapping("/images/clear")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "清理图片处理相关数据", description = "清理Redis队列中的图片处理任务和相关缓存数据")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "清理成功",
                content = @Content(schema = @Schema(implementation = Map.class))),
        @ApiResponse(
                responseCode = "400",
                description = "清理时出错",
                content = @Content(schema = @Schema(implementation = Map.class)))
    })
    public ResponseEntity<CommonResp<Map<String, Object>>> clearImageProcessingData() {
        log.info("Admin request to clear image processing data");

        try {
            asyncService.clearImageProcessingData();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Successfully cleared image processing data");

            return ResponseEntity.ok(CommonResp.success(response));

        } catch (Exception e) {
            log.error("Error clearing image processing data", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(CommonResp.error(e.getMessage()));
        }
    }
}
