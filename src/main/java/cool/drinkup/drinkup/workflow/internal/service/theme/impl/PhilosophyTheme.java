package cool.drinkup.drinkup.workflow.internal.service.theme.impl;

import cool.drinkup.drinkup.shared.enums.CardStyleEnum;
import cool.drinkup.drinkup.shared.enums.ThemeEnum;
import cool.drinkup.drinkup.workflow.internal.repository.ThemeSettingsRepository;
import cool.drinkup.drinkup.workflow.internal.service.theme.Theme;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PhilosophyTheme implements Theme {

    private final ThemeEnum type = ThemeEnum.PHILOSOPHY;

    private final ThemeSettingsRepository themeSettingsRepository;

    @Override
    public String getName() {
        return this.type.name();
    }

    @Override
    public String getThemeContent() {
        var themeSettings = themeSettingsRepository
                .findByType(this.type)
                .orElseThrow(() -> new RuntimeException("Theme not found"));
        return themeSettings.getThemeContent();
    }

    @Override
    public String getThemeImageConfig() {
        var themeSettings = themeSettingsRepository
                .findByType(this.type)
                .orElseThrow(() -> new RuntimeException("Theme not found"));
        return themeSettings.getImageConfig();
    }

    @Override
    public String getImagePrompt() {
        var themeSettings = themeSettingsRepository
                .findByType(this.type)
                .orElseThrow(() -> new RuntimeException("Theme not found"));
        return themeSettings.getImagePrompt();
    }

    @Override
    public CardStyleEnum getCardStyle() {
        // Philosophy主题对应的卡片样式
        CardStyleEnum[] philosophyStyles = {CardStyleEnum.PHILOSOPHY_1, CardStyleEnum.PHILOSOPHY_2};
        return philosophyStyles[new java.security.SecureRandom().nextInt(philosophyStyles.length)];
    }
}
