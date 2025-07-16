package cool.drinkup.drinkup.wine.internal.controller;

import cool.drinkup.drinkup.shared.spi.CommonResp;
import cool.drinkup.drinkup.user.spi.AuthenticationServiceFacade;
import cool.drinkup.drinkup.wine.internal.controller.resp.PropagateResp;
import cool.drinkup.drinkup.wine.internal.service.PropagateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/propagate")
@RequiredArgsConstructor
@Tag(name = "酒卡分享", description = "提供酒卡分享相关的API接口")
public class PropagateController {
    private final PropagateService propagateService;
    private final AuthenticationServiceFacade authenticationServiceFacade;

    @Operation(summary = "生成酒卡分享ID", description = "生成酒卡分享ID")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "成功生成酒卡分享ID"),
                @ApiResponse(responseCode = "404", description = "未找到指定ID的酒卡")
            })
    @PreAuthorize("isAuthenticated()")
    @PostMapping()
    public ResponseEntity<CommonResp<PropagateResp>> propagateWine(@RequestBody PropagateRequest request) {
        authenticationServiceFacade.getCurrentAuthenticatedUser().ifPresent(user -> request.setUserId(user.userId()));
        String sharedId = propagateService.makeSharedId(request);
        PropagateResp resp = new PropagateResp();
        resp.setSharedId(sharedId);
        return ResponseEntity.ok(CommonResp.success(resp));
    }

    @Operation(summary = "根据酒卡分享ID获取酒卡信息", description = "根据酒卡分享ID获取酒卡信息")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "成功获取酒卡信息"),
                @ApiResponse(responseCode = "404", description = "未找到指定ID的酒卡")
            })
    @GetMapping("/{sharedId}")
    public ResponseEntity<CommonResp<Object>> getSharedInfoBySharedId(@PathVariable String sharedId) {
        Object sharedInfo = propagateService.getSharedInfoBySharedId(sharedId);
        return ResponseEntity.ok(CommonResp.success(sharedInfo));
    }
}
