package cool.drinkup.drinkup.workflow.internal.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cool.drinkup.drinkup.shared.spi.CommonResp;
import cool.drinkup.drinkup.workflow.internal.controller.resp.MaterialCategoryVo;
import cool.drinkup.drinkup.workflow.internal.mapper.MaterialCategoryMapper;
import cool.drinkup.drinkup.workflow.internal.model.MaterialCategory;
import cool.drinkup.drinkup.workflow.internal.service.material.MaterialCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "材料分类管理", description = "材料分类相关接口")
@RestController
@RequestMapping("/api/material-category")
@RequiredArgsConstructor
public class MaterialCategoryController {

    private final MaterialCategoryService materialCategoryService;
    private final MaterialCategoryMapper materialCategoryMapper;

    @Operation(summary = "获取所有分类", description = "获取当前所有的材料分类（树形结构）")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommonResp<List<MaterialCategoryVo>>> getAllCategories(
            @Parameter(description = "是否返回树形结构", example = "true") 
            @RequestParam(defaultValue = "false") Boolean tree) {
        
        List<MaterialCategory> categories;
        if (tree) {
            categories = materialCategoryService.getAllCategories();
            List<MaterialCategoryVo> categoryVos = buildCategoryTree(categories);
            return ResponseEntity.ok(CommonResp.success(categoryVos));
        } else {
            categories = materialCategoryService.getAllCategories();
            List<MaterialCategoryVo> categoryVos = categories.stream()
                    .map(materialCategoryMapper::toMaterialCategoryVo)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(CommonResp.success(categoryVos));
        }
    }

    @Operation(summary = "获取顶级分类", description = "获取所有一级分类")
    @GetMapping("/top-level")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommonResp<List<MaterialCategoryVo>>> getTopLevelCategories() {
        List<MaterialCategory> categories = materialCategoryService.getTopLevelCategories();
        List<MaterialCategoryVo> categoryVos = categories.stream()
                .map(materialCategoryMapper::toMaterialCategoryVo)
                .collect(Collectors.toList());
        return ResponseEntity.ok(CommonResp.success(categoryVos));
    }

    @Operation(summary = "获取子分类", description = "根据父分类ID获取子分类")
    @GetMapping("/{parentId}/children")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommonResp<List<MaterialCategoryVo>>> getCategoriesByParentId(
            @Parameter(description = "父分类ID") @PathVariable Long parentId) {
        List<MaterialCategory> categories = materialCategoryService.getCategoriesByParentId(parentId);
        List<MaterialCategoryVo> categoryVos = categories.stream()
                .map(materialCategoryMapper::toMaterialCategoryVo)
                .collect(Collectors.toList());
        return ResponseEntity.ok(CommonResp.success(categoryVos));
    }

    /**
     * 构建分类树
     */
    private List<MaterialCategoryVo> buildCategoryTree(List<MaterialCategory> allCategories) {
        List<MaterialCategoryVo> allCategoryVos = allCategories.stream()
                .map(materialCategoryMapper::toMaterialCategoryVo)
                .collect(Collectors.toList());

        Map<Long, List<MaterialCategoryVo>> categoryMap = allCategoryVos.stream()
                .filter(category -> category.getParentId() != null)
                .collect(Collectors.groupingBy(MaterialCategoryVo::getParentId));

        // 设置子分类
        for (MaterialCategoryVo categoryVo : allCategoryVos) {
            List<MaterialCategoryVo> children = categoryMap.get(categoryVo.getId());
            categoryVo.setChildren(children);
        }

        // 返回顶级分类
        return allCategoryVos.stream()
                .filter(category -> category.getParentId() == null)
                .collect(Collectors.toList());
    }
} 