package cool.drinkup.drinkup.workflow.internal.service.stock;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import cool.drinkup.drinkup.workflow.internal.controller.bar.req.BarStockCreateReq;
import cool.drinkup.drinkup.workflow.internal.controller.bar.req.BarStockUpdateReq;
import cool.drinkup.drinkup.workflow.internal.mapper.BatStockMapper;
import cool.drinkup.drinkup.workflow.internal.model.BarStock;
import cool.drinkup.drinkup.workflow.internal.repository.BarStockRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BarStockService {
    
    private final BarStockRepository barStockRepository;

    private final BatStockMapper batStockMapper;
    
    public List<BarStock> getBarStock(Long barId) {
        return barStockRepository.findByBarId(barId);
    }

    public List<BarStock> createBarStock(Long barId, BarStockCreateReq barStockCreateReq) {
        List<BarStock> barStocks = batStockMapper.toBarStock(barStockCreateReq, barId);
        return barStockRepository.saveAll(barStocks);
    }

    @Transactional
    public BarStock updateBarStock(Long barId, Long stockId, BarStockUpdateReq barStockUpdateReq) {
        BarStock barStock = barStockRepository.findByIdAndBarId(stockId, barId)
                .orElseThrow(() -> new RuntimeException("Bar stock not found"));
        
        // Update fields if provided in the request
        if (barStockUpdateReq.getName() != null) {
            barStock.setName(barStockUpdateReq.getName());
        }
        if ( barStockUpdateReq.getNameEn() != null) {
            barStock.setNameEn(barStockUpdateReq.getNameEn());
        }
        if (barStockUpdateReq.getType() != null) {
            barStock.setType(barStockUpdateReq.getType());
        }
        if (barStockUpdateReq.getDescription() != null) {
            barStock.setDescription(barStockUpdateReq.getDescription());
        }
        
        return barStockRepository.save(barStock);
    }

    @Transactional
    public void deleteBarStock(Long barId, Long stockId) {
        BarStock barStock = barStockRepository.findByIdAndBarId(stockId, barId)
                .orElseThrow(() -> new RuntimeException("Bar stock not found"));
        barStockRepository.delete(barStock);
    }

    @Transactional
    public List<BarStock> saveAll(List<BarStock> barStocks) {
        return barStockRepository.saveAll(barStocks);
    }

    public BarStock getBarStockById(Long stockId) {
        return barStockRepository.findById(stockId)
                .orElseThrow(() -> new RuntimeException("Bar stock not found"));
    }
}
