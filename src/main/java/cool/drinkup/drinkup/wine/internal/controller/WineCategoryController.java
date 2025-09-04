package cool.drinkup.drinkup.wine.internal.controller;

import cool.drinkup.drinkup.shared.spi.CommonResp;
import cool.drinkup.drinkup.wine.internal.controller.resp.WineCategoryVo;
import cool.drinkup.drinkup.wine.internal.controller.resp.WineSceneCategoryTreeVo;
import cool.drinkup.drinkup.wine.internal.controller.resp.WineSceneVo;
import cool.drinkup.drinkup.wine.internal.controller.resp.WorkflowWineVo;
import cool.drinkup.drinkup.wine.internal.mapper.WineCategoryMapper;
import cool.drinkup.drinkup.wine.internal.mapper.WineMapper;
import cool.drinkup.drinkup.wine.internal.model.Wine;
import cool.drinkup.drinkup.wine.internal.model.WineCategory;
import cool.drinkup.drinkup.wine.internal.model.WineScene;
import cool.drinkup.drinkup.wine.internal.service.WineCategoryService;
import cool.drinkup.drinkup.wine.internal.service.WineSceneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wine-categories")
@RequiredArgsConstructor
@Tag(name = "酒类分类管理", description = "提供基于场景的酒类分类查询API")
public class WineCategoryController {

    private final WineSceneService sceneService;
    private final WineCategoryService categoryService;
    private final WineMapper wineMapper;
    private final WineCategoryMapper categoryMapper;

    // ========== 场景相关接口 ==========

    @GetMapping("/scenes")
    @Operation(summary = "获取所有场景")
    @ApiResponse(responseCode = "200", description = "成功获取所有场景")
    public ResponseEntity<CommonResp<List<WineSceneVo>>> getAllScenes() {
        List<WineScene> scenes = sceneService.getAllScenes();
        List<WineSceneVo> sceneVos = categoryMapper.toSceneVoList(scenes);
        return ResponseEntity.ok(CommonResp.success(sceneVos));
    }

    @GetMapping("/scenes/{sceneId}")
    @Operation(summary = "根据ID获取场景详情")
    @Parameter(name = "sceneId", description = "场景ID", required = true)
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "成功获取场景详情"),
                @ApiResponse(responseCode = "404", description = "场景不存在")
            })
    public ResponseEntity<CommonResp<WineSceneVo>> getSceneById(@PathVariable Long sceneId) {
        WineScene scene = sceneService.getSceneById(sceneId);
        return ResponseEntity.ok(CommonResp.success(categoryMapper.toSceneVo(scene)));
    }

    // ========== 分类树相关接口 ==========

    @GetMapping("/tree")
    @Operation(summary = "获取所有场景的完整分类树")
    @ApiResponse(responseCode = "200", description = "成功获取完整分类树")
    public ResponseEntity<CommonResp<List<WineSceneCategoryTreeVo>>> getAllCategoryTrees() {
        List<WineSceneCategoryTreeVo> trees = categoryService.getAllSceneCategoryTrees();
        return ResponseEntity.ok(CommonResp.success(trees));
    }

    @GetMapping("/scenes/{sceneId}/tree")
    @Operation(summary = "获取指定场景的分类树")
    @Parameter(name = "sceneId", description = "场景ID", required = true)
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "成功获取场景分类树"),
                @ApiResponse(responseCode = "404", description = "场景不存在")
            })
    public ResponseEntity<CommonResp<WineSceneCategoryTreeVo>> getSceneCategoryTree(@PathVariable Long sceneId) {
        WineSceneCategoryTreeVo tree = categoryService.getSceneCategoryTree(sceneId);
        return ResponseEntity.ok(CommonResp.success(tree));
    }

    // ========== 分类查询接口 ==========

    @GetMapping("/scenes/{sceneId}/categories")
    @Operation(summary = "获取场景下的一级分类")
    @Parameter(name = "sceneId", description = "场景ID", required = true)
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "成功获取一级分类列表"),
                @ApiResponse(responseCode = "404", description = "场景不存在")
            })
    public ResponseEntity<CommonResp<List<WineCategoryVo>>> getLevel1CategoriesByScene(@PathVariable Long sceneId) {
        List<WineCategory> categories = categoryService.getLevel1CategoriesByScene(sceneId);
        List<WineCategoryVo> categoryVos = categoryMapper.toCategoryVoList(categories);
        return ResponseEntity.ok(CommonResp.success(categoryVos));
    }

    @GetMapping("/categories/{categoryId}/subcategories")
    @Operation(summary = "获取一级分类下的二级分类")
    @Parameter(name = "categoryId", description = "一级分类ID", required = true)
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "成功获取二级分类列表"),
                @ApiResponse(responseCode = "404", description = "分类不存在")
            })
    public ResponseEntity<CommonResp<List<WineCategoryVo>>> getLevel2Categories(@PathVariable Long categoryId) {
        List<WineCategory> categories = categoryService.getLevel2CategoriesByParent(categoryId);
        List<WineCategoryVo> categoryVos = categoryMapper.toCategoryVoList(categories);
        return ResponseEntity.ok(CommonResp.success(categoryVos));
    }

    // ========== 酒单查询接口 ==========

    @GetMapping("/scenes/{sceneId}/wines")
    @Operation(summary = "根据场景查询酒单", description = "查询指定场景下所有分类的酒")
    @Parameter(name = "sceneId", description = "场景ID", required = true)
    @Parameter(name = "page", description = "页码，从0开始", required = false)
    @Parameter(name = "size", description = "每页大小", required = false)
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "成功获取酒单"),
                @ApiResponse(responseCode = "404", description = "场景不存在")
            })
    public ResponseEntity<CommonResp<Page<WorkflowWineVo>>> getWinesByScene(
            @PathVariable Long sceneId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Wine> wines = categoryService.getWinesByScene(sceneId, pageRequest);
        Page<WorkflowWineVo> wineVos = wines.map(wineMapper::toWineVo);
        return ResponseEntity.ok(CommonResp.success(wineVos));
    }

    @GetMapping("/categories/{categoryId}/wines")
    @Operation(summary = "根据分类查询酒单", description = "支持一级分类和二级分类，会包含子分类的酒")
    @Parameter(name = "categoryId", description = "分类ID", required = true)
    @Parameter(name = "page", description = "页码，从0开始", required = false)
    @Parameter(name = "size", description = "每页大小", required = false)
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "成功获取酒单"),
                @ApiResponse(responseCode = "404", description = "分类不存在")
            })
    public ResponseEntity<CommonResp<Page<WorkflowWineVo>>> getWinesByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Wine> wines = categoryService.getWinesByCategory(categoryId, pageRequest);
        Page<WorkflowWineVo> wineVos = wines.map(wineMapper::toWineVo);
        return ResponseEntity.ok(CommonResp.success(wineVos));
    }
}
