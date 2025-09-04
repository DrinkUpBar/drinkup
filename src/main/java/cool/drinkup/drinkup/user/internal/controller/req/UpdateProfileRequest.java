package cool.drinkup.drinkup.user.internal.controller.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "更新个人资料请求")
public class UpdateProfileRequest {

    @NotBlank(message = "昵称不能为空")
    @Schema(description = "用户昵称")
    private String nickname;

    // 未来可以添加其他个人资料字段，如头像、电话等
}
