package cool.drinkup.drinkup.workflow.internal.service.theme;

import cool.drinkup.drinkup.shared.enums.ThemeEnum;
import cool.drinkup.drinkup.workflow.internal.service.theme.impl.CyberWorkTheme;
import cool.drinkup.drinkup.workflow.internal.service.theme.impl.MovieTheme;
import cool.drinkup.drinkup.workflow.internal.service.theme.impl.PhilosophyTheme;
import cool.drinkup.drinkup.workflow.internal.service.theme.impl.RandomTheme;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ThemeFactory {

    private final CyberWorkTheme cyberWorkTheme;
    private final PhilosophyTheme philosophyTheme;
    private final MovieTheme movieTheme;
    private final RandomTheme randomTheme;

    public Theme getTheme(@NotNull ThemeEnum themeEnum) {
        return switch (themeEnum) {
            case PHILOSOPHY -> philosophyTheme;
            case CYBER_WORK -> cyberWorkTheme;
            case MOVIE -> movieTheme;
            case RANDOM -> randomTheme;
        };
    }
}
