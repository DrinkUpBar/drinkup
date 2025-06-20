package cool.drinkup.drinkup.user.internal.controller.req;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "用户登录请求体")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginRequest {

    @NotNull(message = "登录类型不能为空")
    @Schema(description = "登录类型", example = "SMS", allowableValues = { "SMS", "GOOGLE", "APPLE" })
    private LoginType loginType = LoginType.SMS;

    // 手机号登录相关字段
    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "验证码", example = "123456")
    private String verificationCode;

    @Schema(description = "Google ID 令牌")
    private String idToken;

    // 枚举定义登录类型
    public enum LoginType {
        SMS("手机号验证码登录"),
        GOOGLE("Google 登录"),
        APPLE("Apple 登录");

        private final String description;

        LoginType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
} 