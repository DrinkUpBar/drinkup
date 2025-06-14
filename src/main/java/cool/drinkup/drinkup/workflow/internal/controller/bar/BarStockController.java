package cool.drinkup.drinkup.workflow.internal.controller.bar;

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

import java.util.List;
import java.util.stream.Collectors;

import cool.drinkup.drinkup.shared.spi.CommonResp;
import cool.drinkup.drinkup.workflow.internal.controller.bar.req.BarStockCreateReq;
import cool.drinkup.drinkup.workflow.internal.controller.bar.req.BarStockUpdateReq;
import cool.drinkup.drinkup.workflow.internal.controller.bar.resp.BarStockVo;
import cool.drinkup.drinkup.workflow.internal.mapper.BatStockMapper;
import cool.drinkup.drinkup.workflow.internal.model.BarStock;
import cool.drinkup.drinkup.workflow.internal.service.stock.BarStockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "吧台库存管理", description = "吧台库存相关接口")
@RestController
@RequestMapping("/bar-stock")
@RequiredArgsConstructor
public class BarStockController {

    private final BarStockService barStockService;

    private final BatStockMapper batStockMapper;

    @Operation(summary = "获取吧台库存", description = "获取指定吧台的所有库存信息")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{barId}")
    public ResponseEntity<CommonResp<List<BarStockVo>>> getBarStock(
        @Parameter(description = "吧台ID") @PathVariable Long barId) {
        List<BarStock> barStock = barStockService.getBarStock(barId);
        return ResponseEntity.ok(
            CommonResp.success(
                barStock.stream().map(batStockMapper::toBarStockVo)
                    .collect(Collectors.toList())
            ));
    }

    @Operation(summary = "创建吧台库存", description = "为指定吧台创建新的库存记录")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{barId}")
    public ResponseEntity<CommonResp<List<BarStockVo>>> createBarStock(
        @Parameter(description = "吧台ID") @PathVariable Long barId,
        @RequestBody BarStockCreateReq barStockCreateReq) {
        List<BarStock> createdBarStocks = barStockService.createBarStock(barId, barStockCreateReq);
        return ResponseEntity.ok(
            CommonResp.success(
                createdBarStocks.stream()
                    .map(batStockMapper::toBarStockVo)
                    .collect(Collectors.toList())
            ));
    }

    @Operation(summary = "更新吧台库存", description = "更新指定吧台的库存记录")
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{barId}/{stockId}")
    public ResponseEntity<CommonResp<BarStockVo>> updateBarStock(
        @Parameter(description = "吧台ID") @PathVariable Long barId,
        @Parameter(description = "库存ID") @PathVariable Long stockId,
        @RequestBody BarStockUpdateReq barStockUpdateReq) {
        BarStock updatedBarStock = barStockService.updateBarStock(barId, stockId, barStockUpdateReq);
        return ResponseEntity.ok(
            CommonResp.success(
                batStockMapper.toBarStockVo(updatedBarStock)
            ));
    }

    @Operation(summary = "删除吧台库存", description = "删除指定吧台的库存记录")
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{barId}/{stockId}")
    public ResponseEntity<CommonResp<Void>> deleteBarStock(
        @Parameter(description = "吧台ID") @PathVariable Long barId,
        @Parameter(description = "库存ID") @PathVariable Long stockId) {
        barStockService.deleteBarStock(barId, stockId);
        return ResponseEntity.ok(CommonResp.success(null));
    }
}
