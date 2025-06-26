package cool.drinkup.drinkup.wine.spi;

import cool.drinkup.drinkup.wine.internal.controller.resp.WorkflowWineVo;
import cool.drinkup.drinkup.wine.internal.model.Wine;
import jakarta.annotation.Nullable;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WineServiceFacade {
    @Nullable
    Wine getWineById(Long id);

    Page<Wine> getWinesByTag(String tagMainBaseSpirit, String tagIba, Pageable pageable);

    @Nullable
    Wine getRandomWine();

    List<Wine> getRandomWines(int count);

    WorkflowWineResp processCocktailRequest(String userInput);

    WorkflowWineVo toWineVo(Wine wine);
}
