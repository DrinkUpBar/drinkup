package cool.drinkup.drinkup.workflow.internal.service.bar;

import cool.drinkup.drinkup.workflow.internal.controller.bar.req.BarCreateReq;
import cool.drinkup.drinkup.workflow.internal.controller.bar.req.BarUpdateReq;
import cool.drinkup.drinkup.workflow.internal.mapper.BarMapper;
import cool.drinkup.drinkup.workflow.internal.model.Bar;
import cool.drinkup.drinkup.workflow.internal.repository.BarRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
@RequiredArgsConstructor
public class BarService {

    private final BarMapper barMapper;
    private final BarRepository barRepository;

    public Bar createBar(BarCreateReq barCreateReq) {
        Bar bar = barMapper.toBar(barCreateReq);
        return barRepository.save(bar);
    }

    @Transactional(readOnly = true)
    public List<Bar> getUserBar(Long userId) {
        return barRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<Bar> getUserBarByBarIds(List<Long> barIds) {
        if (CollectionUtils.isEmpty(barIds)) {
            return new ArrayList<>();
        }
        return barRepository.findAllById(barIds);
    }

    @Transactional
    public Bar updateBar(Long barId, BarUpdateReq barUpdateReq) {
        return barRepository
                .findById(barId)
                .map(existingBar -> {
                    if (barUpdateReq.getName() != null) {
                        existingBar.setName(barUpdateReq.getName());
                    }
                    if (barUpdateReq.getDescription() != null) {
                        existingBar.setDescription(barUpdateReq.getDescription());
                    }
                    if (barUpdateReq.getImage() != null) {
                        existingBar.setImage(barUpdateReq.getImage());
                    }
                    return barRepository.save(existingBar);
                })
                .orElseThrow(() -> new RuntimeException("Bar not found with id: " + barId));
    }

    @Transactional
    public void deleteBar(Long barId) {
        barRepository.deleteById(barId);
    }

    @Transactional(readOnly = true)
    public Optional<Bar> getBarById(Long barId) {
        return barRepository.findById(barId);
    }
}
