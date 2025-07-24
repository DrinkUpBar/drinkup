package cool.drinkup.drinkup.workflow.internal.service.theme;

import cool.drinkup.drinkup.shared.enums.CardStyleEnum;

public interface Theme {

    public String getName();

    public String getThemeImageConfig();

    public String getImagePrompt();

    public CardStyleEnum getCardStyle();
}
