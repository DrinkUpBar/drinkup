package cool.drinkup.drinkup.wine.internal.repository;

import cool.drinkup.drinkup.wine.internal.model.ShareMapping;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShareMappingRepository extends JpaRepository<ShareMapping, Long> {

    ShareMapping findBySharedId(String sharedId);
}
