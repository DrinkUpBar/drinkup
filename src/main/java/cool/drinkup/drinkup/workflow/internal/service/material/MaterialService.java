package cool.drinkup.drinkup.workflow.internal.service.material;

import cool.drinkup.drinkup.workflow.internal.model.Material;
import cool.drinkup.drinkup.workflow.internal.repository.MaterialRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MaterialService {

    private final MaterialRepository materialRepository;

    /**
     * 根据分类ID获取材料列表
     */
    public List<Material> getMaterialsByCategoryId(Long categoryId) {
        return materialRepository.findByCategoryIdAndIsActiveTrueOrderBySortOrderAsc(categoryId);
    }

    /**
     * 获取所有活跃的材料
     */
    public List<Material> getAllMaterials() {
        return materialRepository.findAllActiveMaterialsOrderByCategoryAndSort();
    }

    /**
     * 根据分类ID列表获取材料
     */
    public List<Material> getMaterialsByCategoryIds(List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return getAllMaterials();
        }
        return materialRepository.findByCategoryIds(categoryIds);
    }

    public Material getMaterialById(Long materialId) {
        return materialRepository.findById(materialId).orElse(null);
    }
}
