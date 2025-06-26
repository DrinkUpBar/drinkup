package cool.drinkup.drinkup.wine.spi;

import cool.drinkup.drinkup.shared.dto.UserWine;
import cool.drinkup.drinkup.shared.dto.WorkflowBartenderChatDto;

public interface UserWineServiceFacade {
    UserWine saveUserWine(WorkflowBartenderChatDto chatBotResponse);
}
