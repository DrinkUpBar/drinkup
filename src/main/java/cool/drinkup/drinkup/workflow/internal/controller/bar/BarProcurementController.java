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
import cool.drinkup.drinkup.workflow.internal.controller.bar.req.BarProcurementCreateReq;
import cool.drinkup.drinkup.workflow.internal.controller.bar.req.BarProcurementUpdateReq;
import cool.drinkup.drinkup.workflow.internal.controller.bar.resp.BarProcurementVo;
import cool.drinkup.drinkup.workflow.internal.mapper.BarProcurementMapper;
import cool.drinkup.drinkup.workflow.internal.model.BarProcurement;
import cool.drinkup.drinkup.workflow.internal.service.procurement.BarProcurementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "吧台采购管理", description = "吧台采购相关接口")
@RestController
@RequiredArgsConstructor
@RequestMapping("/bar-procurement")
public class BarProcurementController {

    private final BarProcurementService barProcurementService;

    private final BarProcurementMapper barProcurementMapper;

    @Operation(summary = "获取吧台采购", description = "获取指定吧台的所有采购信息")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{barId}")
    public ResponseEntity<CommonResp<List<BarProcurementVo>>> getBarProcurement(
        @Parameter(description = "吧台ID") @PathVariable Long barId) {
        List<BarProcurement> barProcurement = barProcurementService.getBarProcurement(barId);
        return ResponseEntity.ok(
            CommonResp.success(
                barProcurement.stream()
                    .map(barProcurementMapper::toBarProcurementVo).collect(Collectors.toList())
            ));
    }

    @Operation(summary = "创建吧台采购", description = "为指定吧台创建新的采购记录")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{barId}")
    public ResponseEntity<CommonResp<List<BarProcurementVo>>> createBarProcurement(
        @Parameter(description = "吧台ID") @PathVariable Long barId,
        @RequestBody BarProcurementCreateReq barProcurementCreateReq) {
        List<BarProcurement> createdBarProcurements = barProcurementService.createBarProcurement(barId,
            barProcurementCreateReq);
        return ResponseEntity.ok(
            CommonResp.success(
                createdBarProcurements.stream()
                    .map(barProcurementMapper::toBarProcurementVo)
                    .collect(Collectors.toList())
            ));
    }

    @Operation(summary = "更新吧台采购", description = "更新指定吧台的采购记录")
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{barId}/{procurementId}")
    public ResponseEntity<CommonResp<BarProcurementVo>> updateBarProcurement(
        @Parameter(description = "吧台ID") @PathVariable Long barId,
        @Parameter(description = "采购ID") @PathVariable Long procurementId,
        @RequestBody BarProcurementUpdateReq barProcurementUpdateReq) {
        BarProcurement updatedBarProcurement = barProcurementService.updateBarProcurement(barId, procurementId,
            barProcurementUpdateReq);
        return ResponseEntity.ok(
            CommonResp.success(
                barProcurementMapper.toBarProcurementVo(updatedBarProcurement)
            ));
    }

    @Operation(summary = "删除吧台采购", description = "删除指定吧台的采购记录")
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{barId}/{procurementId}")
    public ResponseEntity<CommonResp<Void>> deleteBarProcurement(
        @Parameter(description = "吧台ID") @PathVariable Long barId,
        @Parameter(description = "采购ID") @PathVariable Long procurementId) {
        barProcurementService.deleteBarProcurement(barId, procurementId);
        return ResponseEntity.ok(CommonResp.success(null));
    }
} 