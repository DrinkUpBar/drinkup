package cool.drinkup.drinkup.workflow.internal.util;

import cool.drinkup.drinkup.workflow.internal.model.Bar;
import cool.drinkup.drinkup.workflow.internal.model.Material;
import cool.drinkup.drinkup.workflow.internal.model.MaterialCategory;
import cool.drinkup.drinkup.workflow.internal.repository.BarRepository;
import cool.drinkup.drinkup.workflow.internal.repository.MaterialCategoryRepository;
import cool.drinkup.drinkup.workflow.internal.repository.MaterialRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
@RequiredArgsConstructor
public class StockDescriptionUtil {

    private final BarRepository barRepository;
    private final MaterialRepository materialRepository;
    private final MaterialCategoryRepository materialCategoryRepository;

    public String getBarStockDescription(Long barId, List<Long> selectedStockIdList) {
        Optional<Bar> byId = barRepository.findById(barId);
        return byId.map(bar -> {
                    StringBuilder description = new StringBuilder();
                    description.append("库存名称: ").append(bar.getName()).append("\n");
                    description.append("库存材料: ");
                    String filteredStocks = bar.getBarStocks().stream()
                            .filter(barStock -> {
                                if (CollectionUtils.isEmpty(selectedStockIdList)) {
                                    return true;
                                }
                                return selectedStockIdList.contains(barStock.getId());
                            })
                            .map(barStock -> barStock.getBarStockDescription())
                            .collect(Collectors.joining(", "));

                    description.append(filteredStocks);
                    return description.toString();
                })
                .orElseThrow(() -> new RuntimeException("Bar not found with id: " + barId));
    }

    public String getMaterialStockDescription(Long categoryId, List<Long> selectedMaterialIdList) {
        Optional<MaterialCategory> byId = materialCategoryRepository.findById(categoryId);
        String categoryName = byId.map(MaterialCategory::getName).orElse("");
        return materialRepository.findByCategoryIdAndIsActiveTrueOrderBySortOrderAsc(categoryId).stream()
                .filter(material -> selectedMaterialIdList.contains(material.getId()))
                .map(Material::getName)
                .collect(Collectors.joining(", ", "用户选取的" + categoryName + "材料：", ""));
    }
}
