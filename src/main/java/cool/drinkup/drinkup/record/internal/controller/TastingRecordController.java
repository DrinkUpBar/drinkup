package cool.drinkup.drinkup.record.internal.controller;

import cool.drinkup.drinkup.record.internal.controller.req.AddTastingRecordRequest;
import cool.drinkup.drinkup.record.internal.controller.req.UpdateTastingRecordRequest;
import cool.drinkup.drinkup.record.internal.controller.resp.TastingRecordResp;
import cool.drinkup.drinkup.record.internal.mapper.TastingRecordMapper;
import cool.drinkup.drinkup.record.internal.model.TastingRecord;
import cool.drinkup.drinkup.record.internal.service.TastingRecordService;
import cool.drinkup.drinkup.shared.spi.CommonResp;
import cool.drinkup.drinkup.user.spi.AuthenticatedUserDTO;
import cool.drinkup.drinkup.user.spi.AuthenticationServiceFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/tasting-record")
@RequiredArgsConstructor
@Tag(name = "品鉴记录管理", description = "品鉴记录的创建和查询功能")
public class TastingRecordController {

    private final TastingRecordService tastingRecordService;
    private final TastingRecordMapper tastingRecordMapper;
    private final AuthenticationServiceFacade authenticationServiceFacade;

    @Operation(summary = "创建品鉴记录", description = "为指定饮品创建品鉴记录")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "创建成功"),
                @ApiResponse(responseCode = "400", description = "请求参数错误"),
                @ApiResponse(responseCode = "401", description = "未授权访问")
            })
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommonResp<TastingRecordResp>> createTastingRecord(
            @Parameter(description = "品鉴记录创建请求") @Valid @RequestBody AddTastingRecordRequest request) {

        try {
            AuthenticatedUserDTO currentUser = authenticationServiceFacade
                    .getCurrentAuthenticatedUser()
                    .orElseThrow(() -> new RuntimeException("用户未登录"));
            TastingRecord tastingRecord = tastingRecordService.createTastingRecord(currentUser.userId(), request);
            TastingRecordResp response = tastingRecordMapper.toTastingRecordResp(tastingRecord);

            log.info("品鉴记录创建成功: userId={}, recordId={}", currentUser.userId(), tastingRecord.getId());
            return ResponseEntity.ok(CommonResp.success(response));
        } catch (Exception e) {
            log.error("创建品鉴记录失败", e);
            return ResponseEntity.ok(CommonResp.error("创建品鉴记录失败: " + e.getMessage()));
        }
    }

    @Operation(summary = "查询品鉴记录", description = "根据饮品ID和类型查询品鉴记录")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "查询成功"),
                @ApiResponse(responseCode = "400", description = "请求参数错误")
            })
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommonResp<List<TastingRecordResp>>> getTastingRecords(
            @Parameter(description = "饮品ID") @RequestParam Long beverageId,
            @Parameter(description = "饮品类型") @RequestParam String beverageType) {

        AuthenticatedUserDTO currentUser = authenticationServiceFacade
                .getCurrentAuthenticatedUser()
                .orElseThrow(() -> new RuntimeException("用户未登录"));

        try {
            List<TastingRecord> tastingRecords =
                    tastingRecordService.getTastingRecordsByBeverage(beverageId, beverageType, currentUser.userId());
            List<TastingRecordResp> response = tastingRecords.stream()
                    .map(tastingRecordMapper::toTastingRecordResp)
                    .collect(Collectors.toList());

            log.info("查询品鉴记录成功: beverageId={}, beverageType={}, count={}", beverageId, beverageType, response.size());
            return ResponseEntity.ok(CommonResp.success(response));
        } catch (Exception e) {
            log.error("查询品鉴记录失败", e);
            return ResponseEntity.ok(CommonResp.error("查询品鉴记录失败: " + e.getMessage()));
        }
    }

    @Operation(summary = "查询我的品鉴记录", description = "查询当前用户的所有品鉴记录")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "查询成功"),
                @ApiResponse(responseCode = "401", description = "未授权访问")
            })
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommonResp<List<TastingRecordResp>>> getMyTastingRecords() {

        try {
            AuthenticatedUserDTO currentUser = authenticationServiceFacade
                    .getCurrentAuthenticatedUser()
                    .orElseThrow(() -> new RuntimeException("用户未登录"));
            List<TastingRecord> tastingRecords = tastingRecordService.getTastingRecordsByUser(currentUser.userId());
            List<TastingRecordResp> response = tastingRecords.stream()
                    .map(tastingRecordMapper::toTastingRecordResp)
                    .collect(Collectors.toList());

            log.info("查询我的品鉴记录成功: userId={}, count={}", currentUser.userId(), response.size());
            return ResponseEntity.ok(CommonResp.success(response));
        } catch (Exception e) {
            log.error("查询我的品鉴记录失败", e);
            return ResponseEntity.ok(CommonResp.error("查询我的品鉴记录失败: " + e.getMessage()));
        }
    }

    @Operation(summary = "修改品鉴记录", description = "修改品鉴记录的内容，只允许修改文字内容，不允许修改图片")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "修改成功"),
                @ApiResponse(responseCode = "400", description = "请求参数错误"),
                @ApiResponse(responseCode = "401", description = "未授权访问"),
                @ApiResponse(responseCode = "404", description = "品鉴记录不存在")
            })
    @PutMapping("/{recordId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommonResp<TastingRecordResp>> updateTastingRecord(
            @Parameter(description = "品鉴记录ID") @PathVariable Long recordId,
            @Parameter(description = "品鉴记录更新请求") @Valid @RequestBody UpdateTastingRecordRequest request) {

        try {
            AuthenticatedUserDTO currentUser = authenticationServiceFacade
                    .getCurrentAuthenticatedUser()
                    .orElseThrow(() -> new RuntimeException("用户未登录"));
            TastingRecord tastingRecord =
                    tastingRecordService.updateTastingRecord(recordId, currentUser.userId(), request);
            TastingRecordResp response = tastingRecordMapper.toTastingRecordResp(tastingRecord);

            log.info("品鉴记录修改成功: userId={}, recordId={}", currentUser.userId(), recordId);
            return ResponseEntity.ok(CommonResp.success(response));
        } catch (Exception e) {
            log.error("修改品鉴记录失败", e);
            return ResponseEntity.ok(CommonResp.error("修改品鉴记录失败: " + e.getMessage()));
        }
    }

    @Operation(summary = "删除品鉴记录", description = "软删除品鉴记录，记录不会被物理删除，只是标记为删除状态")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "删除成功"),
                @ApiResponse(responseCode = "401", description = "未授权访问"),
                @ApiResponse(responseCode = "404", description = "品鉴记录不存在")
            })
    @DeleteMapping("/{recordId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommonResp<String>> deleteTastingRecord(
            @Parameter(description = "品鉴记录ID") @PathVariable Long recordId) {

        try {
            AuthenticatedUserDTO currentUser = authenticationServiceFacade
                    .getCurrentAuthenticatedUser()
                    .orElseThrow(() -> new RuntimeException("用户未登录"));
            tastingRecordService.deleteTastingRecord(recordId, currentUser.userId());

            log.info("品鉴记录删除成功: userId={}, recordId={}", currentUser.userId(), recordId);
            return ResponseEntity.ok(CommonResp.success("品鉴记录删除成功"));
        } catch (Exception e) {
            log.error("删除品鉴记录失败", e);
            return ResponseEntity.ok(CommonResp.error("删除品鉴记录失败: " + e.getMessage()));
        }
    }
}
