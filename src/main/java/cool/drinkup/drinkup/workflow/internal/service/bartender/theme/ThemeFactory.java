package cool.drinkup.drinkup.workflow.internal.service.bartender.theme;

import cool.drinkup.drinkup.workflow.internal.service.bartender.theme.impl.CyberWorkTheme;
import cool.drinkup.drinkup.workflow.internal.service.bartender.theme.impl.DummyTheme;
import cool.drinkup.drinkup.workflow.internal.service.bartender.theme.impl.MovieTheme;
import cool.drinkup.drinkup.workflow.internal.service.bartender.theme.impl.PhilosophyTheme;
import cool.drinkup.drinkup.workflow.internal.service.bartender.theme.impl.RandomTheme;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ThemeFactory {

    private final CyberWorkTheme cyberWorkTheme;
    private final PhilosophyTheme philosophyTheme;
    private final MovieTheme movieTheme;
    private final RandomTheme randomTheme;
    private final DummyTheme dummyTheme;

    public Theme getTheme(@Nullable ThemeEnum themeEnum) {
        if (themeEnum == null) {
            return dummyTheme;
        }
        return switch (themeEnum) {
            case PHILOSOPHY -> philosophyTheme;
            case CYBER_WORK -> cyberWorkTheme;
            case MOVIE -> movieTheme;
            case RANDOM -> randomTheme;
        };
    }
}
