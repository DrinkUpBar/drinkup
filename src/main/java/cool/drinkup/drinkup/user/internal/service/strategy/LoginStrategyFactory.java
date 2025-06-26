package cool.drinkup.drinkup.user.internal.service.strategy;

import cool.drinkup.drinkup.user.internal.controller.req.LoginRequest;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * 登录策略工厂
 * 使用策略模式管理不同的登录方式
 */
@Component
public class LoginStrategyFactory {

    private final Map<LoginRequest.LoginType, LoginStrategy> strategyMap;

    public LoginStrategyFactory(List<LoginStrategy> strategies) {
        this.strategyMap =
                strategies.stream().collect(Collectors.toMap(LoginStrategy::getLoginType, Function.identity()));
    }

    /**
     * 根据登录类型获取对应的策略
     * @param loginType 登录类型
     * @return 登录策略
     * @throws IllegalArgumentException 如果不支持该登录类型
     */
    public LoginStrategy getStrategy(LoginRequest.LoginType loginType) {
        LoginStrategy strategy = strategyMap.get(loginType);
        if (strategy == null) {
            throw new IllegalArgumentException("不支持的登录类型: " + loginType);
        }
        return strategy;
    }

    /**
     * 检查是否支持某种登录类型
     * @param loginType 登录类型
     * @return 是否支持
     */
    public boolean isSupported(LoginRequest.LoginType loginType) {
        return strategyMap.containsKey(loginType);
    }
}
