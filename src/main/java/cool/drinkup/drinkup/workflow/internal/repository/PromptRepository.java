package cool.drinkup.drinkup.workflow.internal.repository;

import cool.drinkup.drinkup.workflow.internal.model.PromptContent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromptRepository extends JpaRepository<PromptContent, Long> {

    PromptContent findByType(String type);
}
