package cool.drinkup.drinkup.favorite.internal.controller;

import com.mzt.logapi.starter.annotation.LogRecord;
import cool.drinkup.drinkup.common.log.event.WineEvent;
import cool.drinkup.drinkup.favorite.internal.controller.req.AddFavoriteRequest;
import cool.drinkup.drinkup.favorite.internal.controller.req.CheckFavoriteMultiBatchRequest;
import cool.drinkup.drinkup.favorite.internal.controller.resp.CheckFavoriteMultiBatchResponse;
import cool.drinkup.drinkup.favorite.internal.dto.UserFavoriteDTO;
import cool.drinkup.drinkup.favorite.internal.service.UserFavoriteService;
import cool.drinkup.drinkup.favorite.spi.ObjectType;
import cool.drinkup.drinkup.shared.spi.CommonResp;
import cool.drinkup.drinkup.user.spi.AuthenticatedUserDTO;
import cool.drinkup.drinkup.user.spi.AuthenticationServiceFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "用户收藏", description = "用户收藏相关接口")
@SecurityRequirement(name = "bearerAuth")
public class UserFavoriteController {

    private final UserFavoriteService favoriteService;
    private final AuthenticationServiceFacade authenticationServiceFacade;

    @LogRecord(
            type = WineEvent.WINE,
            subType = WineEvent.BehaviorEvent.FAVORITE_ADD,
            bizNo = "{{#request.objectType}}-{{#request.objectId}}",
            success = "用户添加{{#request.objectType}}类型收藏成功，对象ID：{{#request.objectId}}")
    @Operation(
            summary = "添加收藏",
            description = "为当前登录用户添加新的收藏项",
            responses = {
                @ApiResponse(responseCode = "200", description = "收藏添加成功"),
                @ApiResponse(responseCode = "400", description = "请求参数无效"),
                @ApiResponse(responseCode = "401", description = "未授权访问")
            })
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommonResp<Void>> addFavorite(@RequestBody @Valid AddFavoriteRequest request) {
        Long userId = getCurrentUserId();
        favoriteService.addFavorite(userId, request.getObjectType(), request.getObjectId(), request.getNote());
        return ResponseEntity.ok(CommonResp.success(null));
    }

    @LogRecord(
            type = WineEvent.WINE,
            subType = WineEvent.BehaviorEvent.FAVORITE_REMOVE,
            bizNo = "{{#objectType}}-{{#objectId}}",
            success = "用户取消{{#objectType}}类型收藏成功，对象ID：{{#objectId}}")
    @Operation(
            summary = "取消收藏",
            description = "删除当前登录用户的收藏项",
            responses = {
                @ApiResponse(responseCode = "200", description = "收藏删除成功"),
                @ApiResponse(responseCode = "401", description = "未授权访问"),
                @ApiResponse(responseCode = "404", description = "收藏项不存在")
            })
    @DeleteMapping("/{objectType}/{objectId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommonResp<Void>> removeFavorite(
            @Parameter(description = "收藏对象类型") @PathVariable ObjectType objectType,
            @Parameter(description = "收藏对象ID") @PathVariable Long objectId) {
        Long userId = getCurrentUserId();
        favoriteService.removeFavorite(userId, objectType, objectId);
        return ResponseEntity.ok(CommonResp.success(null));
    }

    @Operation(
            summary = "获取收藏列表",
            description = "获取当前登录用户的收藏列表（分页）",
            responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "成功获取收藏列表",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = Page.class))),
                @ApiResponse(responseCode = "401", description = "未授权访问")
            })
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommonResp<Page<UserFavoriteDTO>>> getUserFavorites(
            @Parameter(description = "按收藏类型筛选") @RequestParam(required = false) ObjectType objectType,
            @Parameter(description = "分页参数")
                    @PageableDefault(size = 20, sort = "favoriteTime", direction = Sort.Direction.DESC)
                    Pageable pageable) {

        Long userId = getCurrentUserId();
        Page<UserFavoriteDTO> favorites = favoriteService.getUserFavoritesWithDetails(userId, objectType, pageable);
        return ResponseEntity.ok(CommonResp.success(favorites));
    }

    @Operation(
            summary = "检查收藏状态",
            description = "检查当前登录用户是否收藏了指定对象",
            responses = {
                @ApiResponse(responseCode = "200", description = "成功获取收藏状态"),
                @ApiResponse(responseCode = "401", description = "未授权访问")
            })
    @GetMapping("/check/{objectType}/{objectId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommonResp<Boolean>> checkFavorite(
            @Parameter(description = "收藏对象类型") @PathVariable ObjectType objectType,
            @Parameter(description = "收藏对象ID") @PathVariable Long objectId) {
        Long userId = getCurrentUserId();
        boolean isFavorited = favoriteService.isFavorited(userId, objectType, objectId);
        return ResponseEntity.ok(CommonResp.success(isFavorited));
    }

    @Operation(
            summary = "批量检查收藏状态（多类型）",
            description = "批量检查当前登录用户是否收藏了指定的多个对象（支持多种类型）",
            responses = {
                @ApiResponse(responseCode = "200", description = "成功获取批量收藏状态"),
                @ApiResponse(responseCode = "401", description = "未授权访问")
            })
    @PostMapping("/check-batch")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommonResp<CheckFavoriteMultiBatchResponse>> checkFavoriteBatchMulti(
            @Parameter(description = "收藏对象列表") @Valid @RequestBody CheckFavoriteMultiBatchRequest request) {
        Long userId = getCurrentUserId();
        Map<ObjectType, Map<Long, Boolean>> statusMap =
                favoriteService.checkFavoriteStatusMulti(userId, request.getItems());
        CheckFavoriteMultiBatchResponse response = new CheckFavoriteMultiBatchResponse();
        response.setStatusMap(statusMap);
        return ResponseEntity.ok(CommonResp.success(response));
    }

    private Long getCurrentUserId() {
        Optional<AuthenticatedUserDTO> currentAuthenticatedUser =
                authenticationServiceFacade.getCurrentAuthenticatedUser();
        if (currentAuthenticatedUser.isEmpty()) {
            throw new IllegalStateException("Expected authenticated user but got none");
        }
        AuthenticatedUserDTO authenticatedUserDTO = currentAuthenticatedUser.get();
        return authenticatedUserDTO.userId();
    }
}
