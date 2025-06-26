package cool.drinkup.drinkup.workflow.internal.repository;

import cool.drinkup.drinkup.workflow.internal.model.Bar;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BarRepository extends JpaRepository<Bar, Long> {
    List<Bar> findByUserId(Long userId);
}
