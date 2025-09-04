package cool.drinkup.drinkup.workflow.internal.repository;

import cool.drinkup.drinkup.shared.enums.ThemeEnum;
import cool.drinkup.drinkup.workflow.internal.model.ThemeSettings;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ThemeSettingsRepository extends JpaRepository<ThemeSettings, Long> {
    Optional<ThemeSettings> findByType(ThemeEnum type);
}
