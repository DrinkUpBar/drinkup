package cool.drinkup.drinkup.workflow.internal.service.theme.impl;

import cool.drinkup.drinkup.workflow.internal.repository.ThemeSettingsRepository;
import cool.drinkup.drinkup.workflow.internal.service.theme.Theme;
import cool.drinkup.drinkup.workflow.internal.service.theme.ThemeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MovieTheme implements Theme {

    private final ThemeEnum type = ThemeEnum.MOVIE;

    private final ThemeSettingsRepository themeSettingsRepository;

    @Override
    public String getName() {
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
}
