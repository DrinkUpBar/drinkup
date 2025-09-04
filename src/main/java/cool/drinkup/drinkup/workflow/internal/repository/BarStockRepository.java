package cool.drinkup.drinkup.workflow.internal.repository;

import cool.drinkup.drinkup.workflow.internal.model.BarStock;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BarStockRepository extends JpaRepository<BarStock, Long> {

    List<BarStock> findByBarId(Long barId);

    Optional<BarStock> findByIdAndBarId(Long id, Long barId);
}
