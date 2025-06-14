package cool.drinkup.drinkup.workflow.internal.service.procurement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import cool.drinkup.drinkup.workflow.internal.controller.bar.req.BarProcurementCreateReq;
import cool.drinkup.drinkup.workflow.internal.controller.bar.req.BarProcurementUpdateReq;
import cool.drinkup.drinkup.workflow.internal.model.Bar;
import cool.drinkup.drinkup.workflow.internal.model.BarProcurement;
import cool.drinkup.drinkup.workflow.internal.repository.BarProcurementRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BarProcurementService {
    
    @Autowired
    private BarProcurementRepository barProcurementRepository;
    
    /**
     * 获取指定吧台的所有采购信息
     * 
     * @param barId 吧台ID
     * @return 采购信息列表
     */
    public List<BarProcurement> getBarProcurement(Long barId) {
        log.info("获取吧台ID为{}的所有采购信息", barId);
        return barProcurementRepository.findByBarId(barId);
    }
    /**
     * 为指定吧台创建新的采购记录
     * 
     * @param barId 吧台ID
     * @param createReq 创建采购的请求
     * @return 创建的采购记录列表
     */
    @Transactional
    public List<BarProcurement> createBarProcurement(Long barId, BarProcurementCreateReq createReq) {
        log.info("为吧台ID{}创建新的采购记录", barId);

        List<BarProcurement> savedProcurements = new ArrayList<>();
        // 根据请求创建采购记录
        List<BarProcurement> procurements = new ArrayList<>();
        for (BarProcurementCreateReq.InnerBarProcurementCreateReq item : createReq.getBarProcurements()) {
            BarProcurement procurement = new BarProcurement();
            Bar bar = new Bar();
            bar.setId(barId);
            procurement.setBar(bar);
            procurement.setName(item.getName());
            procurement.setType(item.getType());
            procurement.setIconType(item.getIconType());
            procurement.setDescription(item.getDescription());
            procurements.add(procurement);
        }
        
        for (BarProcurement procurement : procurements) {
            savedProcurements.add(barProcurementRepository.save(procurement));
        }
        return savedProcurements;
    }
    
    /**
     * 更新指定吧台的采购记录
     * 
     * @param barId 吧台ID
     * @param procurementId 采购ID
     * @param updateReq 更新采购的请求
     * @return 更新后的采购记录
     */
    @Transactional
    public BarProcurement updateBarProcurement(Long barId, Long procurementId, BarProcurementUpdateReq updateReq) {
        log.info("更新吧台ID{}的采购记录ID{}", barId, procurementId);
        
        // 获取现有的采购记录
        BarProcurement procurement = barProcurementRepository.findByIdAndBarId(procurementId, barId)
                .orElseThrow(() -> new RuntimeException("采购记录不存在"));

        // 更新采购记录的字段，只更新非空字段
        if (updateReq.getName() != null) {
            procurement.setName(updateReq.getName());
        }
        
        if (updateReq.getType() != null) {
            procurement.setType(updateReq.getType());
        }
        
        if (updateReq.getIconType() != null) {
            procurement.setIconType(updateReq.getIconType());
        }
        
        if (updateReq.getDescription() != null) {
            procurement.setDescription(updateReq.getDescription());
        }
        
        // 保存并返回更新后的记录
        return barProcurementRepository.save(procurement);
    }
    
    /**
     * 删除指定吧台的采购记录
     * 
     * @param barId 吧台ID
     * @param procurementId 采购ID
     */
    @Transactional
    public void deleteBarProcurement(Long barId, Long procurementId) {
        log.info("删除吧台ID{}的采购记录ID{}", barId, procurementId);
        
        // 验证采购记录是否存在
        BarProcurement procurement = barProcurementRepository.findByIdAndBarId(procurementId, barId)
                .orElseThrow(() -> new RuntimeException("采购记录不存在"));
        
        // 删除采购记录
        barProcurementRepository.delete(procurement);
    }
} 