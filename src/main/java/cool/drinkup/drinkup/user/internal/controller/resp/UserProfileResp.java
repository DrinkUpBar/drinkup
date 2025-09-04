package cool.drinkup.drinkup.user.internal.controller.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;
import lombok.Data;

@Data
@Schema(description = "用户信息响应")
public class UserProfileResp {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "是否启用")
    private boolean enabled;

    @Schema(description = "用户角色")
    private Set<String> roles;
}
