package cool.drinkup.drinkup.workflow.internal.controller.bar;

import cool.drinkup.drinkup.shared.spi.CommonResp;
import cool.drinkup.drinkup.user.spi.AuthenticatedUserDTO;
import cool.drinkup.drinkup.user.spi.AuthenticationServiceFacade;
import cool.drinkup.drinkup.workflow.internal.controller.bar.req.BarCreateReq;
import cool.drinkup.drinkup.workflow.internal.controller.bar.req.BarUpdateReq;
import cool.drinkup.drinkup.workflow.internal.controller.bar.resp.BarVo;
import cool.drinkup.drinkup.workflow.internal.mapper.BarMapper;
import cool.drinkup.drinkup.workflow.internal.model.Bar;
import cool.drinkup.drinkup.workflow.internal.service.bar.BarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "吧台管理", description = "吧台相关接口")
@RestController
@RequestMapping("/bar")
@RequiredArgsConstructor
public class BarController {

    private final BarService barService;
    private final BarMapper barMapper;
    private final AuthenticationServiceFacade authenticationServiceFacade;

    @Operation(summary = "创建吧台", description = "创建一个新的吧台")
    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<CommonResp<BarVo>> createBar(@RequestBody BarCreateReq barCreateReq) {
        Bar bar = barService.createBar(barCreateReq);
        return ResponseEntity.ok(CommonResp.success(barMapper.toBarVo(bar)));
    }

    @Operation(summary = "获取吧台列表", description = "获取当前用户的所有吧台")
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<CommonResp<List<BarVo>>> getBar() {
        Optional<AuthenticatedUserDTO> currentAuthenticatedUser =
                authenticationServiceFacade.getCurrentAuthenticatedUser();
        if (currentAuthenticatedUser.isEmpty()) {
            throw new RuntimeException("Cannot get current authenticated user");
        }
        AuthenticatedUserDTO authenticatedUserDTO = currentAuthenticatedUser.get();
        List<Bar> bars = barService.getUserBar(authenticatedUserDTO.userId());
        return ResponseEntity.ok(
                CommonResp.success(bars.stream().map(barMapper::toBarVo).collect(Collectors.toList())));
    }

    @Operation(summary = "更新吧台", description = "根据吧台ID更新吧台信息")
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}")
    public ResponseEntity<CommonResp<BarVo>> updateBar(
            @Parameter(description = "吧台ID") @PathVariable("id") Long id, @RequestBody BarUpdateReq barUpdateReq) {
        Bar updatedBar = barService.updateBar(id, barUpdateReq);
        return ResponseEntity.ok(CommonResp.success(barMapper.toBarVo(updatedBar)));
    }

    @Operation(summary = "删除吧台", description = "根据吧台ID删除吧台")
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResp<Void>> deleteBar(@Parameter(description = "吧台ID") @PathVariable("id") Long id) {
        barService.deleteBar(id);
        return ResponseEntity.ok(CommonResp.success(null));
    }

    @Operation(summary = "获取特定吧台", description = "根据吧台ID获取吧台信息")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<CommonResp<BarVo>> getBarById(@Parameter(description = "吧台ID") @PathVariable("id") Long id) {
        Bar bar = barService.getBarById(id).orElseThrow(() -> new RuntimeException("Bar not found with id: " + id));
        return ResponseEntity.ok(CommonResp.success(barMapper.toBarVo(bar)));
    }
}
