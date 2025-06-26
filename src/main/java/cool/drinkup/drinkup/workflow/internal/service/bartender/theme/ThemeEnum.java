package cool.drinkup.drinkup.workflow.internal.service.bartender.theme;

import jakarta.annotation.Nullable;
import java.security.SecureRandom;

public enum ThemeEnum {
    PHILOSOPHY,
    CYBER_WORK,
    MOVIE,
    RANDOM,
    ;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public static @Nullable ThemeEnum fromValue(String value) {
        if (value == null || value.isEmpty()) {
            // 随机一个已有的theme
            return ThemeEnum.values()[SECURE_RANDOM.nextInt(ThemeEnum.values().length)];
        }
        return ThemeEnum.valueOf(value.toUpperCase());
    }
}
