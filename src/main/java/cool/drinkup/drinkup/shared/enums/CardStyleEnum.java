package cool.drinkup.drinkup.shared.enums;

import java.security.SecureRandom;

public enum CardStyleEnum {
    CLASSIC_1,
    CLASSIC_2,
    MOVIE_1,
    MOVIE_2,
    PHILOSOPHY_1,
    PHILOSOPHY_2,
    WORKER_1,
    WORKER_2,
    WORKER_3,
    ZERO_1,
    ZERO_2;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * 随机返回一个卡片样式
     * @return 随机的CardStyleEnum值
     */
    public static CardStyleEnum getRandomCardStyle() {
        CardStyleEnum[] values = values();
        return values[SECURE_RANDOM.nextInt(values.length)];
    }
}
