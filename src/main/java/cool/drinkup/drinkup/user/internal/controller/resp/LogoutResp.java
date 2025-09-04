package cool.drinkup.drinkup.user.internal.controller.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户登出响应")
public class LogoutResp {

    @Schema(description = "登出消息")
    private String message;

    @Schema(description = "登出状态")
    private String status;
}
