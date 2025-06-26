package cool.drinkup.drinkup.user.internal.controller.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "手机号请求体")
public class PhoneNumberRequest {

    @NotBlank(message = "手机号不能为空")
    @Schema(description = "手机号", example = "13800138000")
    private String phoneNumber;
}
