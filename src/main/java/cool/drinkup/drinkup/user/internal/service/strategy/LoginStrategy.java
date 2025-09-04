package cool.drinkup.drinkup.user.internal.service.strategy;

import cool.drinkup.drinkup.user.internal.controller.req.LoginRequest;
import cool.drinkup.drinkup.user.internal.model.User;

/**
 * 登录策略接口 使用策略模式支持多种登录方式
 */
public interface LoginStrategy {

    /**
     * 获取支持的登录类型
     *
     * @return 登录类型
     */
    LoginRequest.LoginType getLoginType();

    /**
     * 验证登录凭据
     *
     * @param loginRequest 登录请求
     * @return 验证是否成功
     */
    boolean validateCredentials(LoginRequest loginRequest);

    /**
     * 获取或创建用户
     *
     * @param loginRequest 登录请求
     * @return 用户对象
     */
    LoginResult getOrCreateUser(LoginRequest loginRequest);

    /**
     * 获取用户唯一标识符（用于查找现有用户）
     *
     * @param loginRequest 登录请求
     * @return 用户唯一标识符
     */
    String getUserIdentifier(LoginRequest loginRequest);

    record LoginResult(User user, boolean isNewUser) {}
}
