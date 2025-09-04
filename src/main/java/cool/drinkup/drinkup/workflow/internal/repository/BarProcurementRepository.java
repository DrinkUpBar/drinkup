package cool.drinkup.drinkup.workflow.internal.repository;

import cool.drinkup.drinkup.workflow.internal.model.BarProcurement;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BarProcurementRepository extends JpaRepository<BarProcurement, Long> {

    List<BarProcurement> findByBarId(Long barId);

    Optional<BarProcurement> findByIdAndBarId(Long procurementId, Long barId);
}
