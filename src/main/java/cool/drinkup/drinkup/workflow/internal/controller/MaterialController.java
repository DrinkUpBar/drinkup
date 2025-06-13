package cool.drinkup.drinkup.workflow.internal.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

import cool.drinkup.drinkup.shared.spi.CommonResp;
import cool.drinkup.drinkup.workflow.internal.controller.resp.MaterialVo;
import cool.drinkup.drinkup.workflow.internal.mapper.MaterialMapper;
import cool.drinkup.drinkup.workflow.internal.model.Material;
import cool.drinkup.drinkup.workflow.internal.service.material.MaterialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "材料管理", description = "材料相关接口")
@RestController
@RequestMapping("/api/material")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialService materialService;
    private final MaterialMapper materialMapper;

    @Operation(summary = "获取全量材料", description = "获取所有活跃的材料")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommonResp<List<MaterialVo>>> getAllMaterials() {
        List<Material> materials = materialService.getAllMaterials();
        List<MaterialVo> materialVos = materials.stream()
                .map(materialMapper::toMaterialVo)
                .collect(Collectors.toList());
        return ResponseEntity.ok(CommonResp.success(materialVos));
    }

    @Operation(summary = "根据分类获取材料", description = "通过分类ID获取分类下的材料")
    @GetMapping("/category/{categoryId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommonResp<List<MaterialVo>>> getMaterialsByCategoryId(
            @Parameter(description = "分类ID") @PathVariable Long categoryId) {
        List<Material> materials = materialService.getMaterialsByCategoryId(categoryId);
        List<MaterialVo> materialVos = materials.stream()
                .map(materialMapper::toMaterialVo)
                .collect(Collectors.toList());
        return ResponseEntity.ok(CommonResp.success(materialVos));
    }

    @Operation(summary = "根据多个分类获取材料", description = "通过多个分类ID获取材料")
    @GetMapping("/categories")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommonResp<List<MaterialVo>>> getMaterialsByCategoryIds(
            @Parameter(description = "分类ID列表，用逗号分隔", example = "1,2,3") @RequestParam List<Long> categoryIds) {
        List<Material> materials = materialService.getMaterialsByCategoryIds(categoryIds);
        List<MaterialVo> materialVos = materials.stream()
                .map(materialMapper::toMaterialVo)
                .collect(Collectors.toList());
        return ResponseEntity.ok(CommonResp.success(materialVos));
    }
}