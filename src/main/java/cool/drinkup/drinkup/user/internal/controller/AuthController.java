package cool.drinkup.drinkup.user.internal.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mzt.logapi.starter.annotation.LogRecord;

import java.util.stream.Collectors;

import cool.drinkup.drinkup.common.log.event.UserEvent;
import cool.drinkup.drinkup.infrastructure.spi.SmsSender;
import cool.drinkup.drinkup.shared.spi.CommonResp;
import cool.drinkup.drinkup.user.internal.controller.req.LoginRequest;
import cool.drinkup.drinkup.user.internal.controller.req.PhoneNumberRequest;
import cool.drinkup.drinkup.user.internal.controller.resp.LogoutResp;
import cool.drinkup.drinkup.user.internal.controller.resp.UserLoginResp;
import cool.drinkup.drinkup.user.internal.mapper.UserMapper;
import cool.drinkup.drinkup.user.internal.model.DrinkupUserDetails;
import cool.drinkup.drinkup.user.internal.model.User;
import cool.drinkup.drinkup.user.internal.service.strategy.LoginStrategy;
import cool.drinkup.drinkup.user.internal.service.strategy.LoginStrategyFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "用户认证相关的接口，包括注册、登录和登出")
public class AuthController {

    private final SmsSender smsSender;
    private final UserMapper userMapper;
    private final LoginStrategyFactory loginStrategyFactory;

    @Operation(summary = "发送验证码", description = "发送验证码到指定手机号")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "验证码发送成功"),
            @ApiResponse(responseCode = "400", description = "验证码发送失败，可能是手机号格式错误或发送次数限制")
    })
    @PostMapping("/send-verification-code")
    public ResponseEntity<CommonResp<String>> sendVerificationCode(
            @Parameter(description = "手机号请求体") @Valid @RequestBody PhoneNumberRequest request) {
        try {
            smsSender.sendSms(request.getPhoneNumber(), generateVerificationCode());
            return ResponseEntity.ok(CommonResp.success("验证码发送成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(CommonResp.error("验证码发送失败: " + e.getMessage()));
        }
    }

    private String generateVerificationCode() {
        // 使用 SecureRandom 生成更安全的6位随机数字验证码
        java.security.SecureRandom secureRandom = new java.security.SecureRandom();
        int code = 100000 + secureRandom.nextInt(900000); // 确保是6位数字
        return String.valueOf(code);
    }

    @LogRecord(
        type = UserEvent.USER,
        subType = "{{#_ret.body.data.isNewUser ? T(cool.drinkup.drinkup.common.log.event.UserEvent$BehaviorEvent).REGISTER : T(cool.drinkup.drinkup.common.log.event.UserEvent$BehaviorEvent).LOGIN}}",
        bizNo = "{{#_ret.body.data.user.id}}",
        success = "用户{{#_ret.body.data.user.id}}登录成功"
    )
    @Operation(summary = "用户登录", description = "支持多种登录方式：手机号验证码登录、Google 登录和 Apple 登录")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "登录成功"),
            @ApiResponse(responseCode = "400", description = "登录失败，参数错误或凭据无效"),
            @ApiResponse(responseCode = "401", description = "登录失败，认证失败")
    })
    @PostMapping("/login")
    public ResponseEntity<CommonResp<UserLoginResp>> login(
            @Parameter(description = "登录信息，支持手机号验证码登录、Google 登录和 Apple 登录") 
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {

        try {
            // 验证登录类型是否支持
            if (!loginStrategyFactory.isSupported(loginRequest.getLoginType())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(CommonResp.error("不支持的登录类型: " + loginRequest.getLoginType()));
            }

            // 获取对应的登录策略
            LoginStrategy loginStrategy = loginStrategyFactory.getStrategy(loginRequest.getLoginType());

            // 验证登录凭据
            if (!loginStrategy.validateCredentials(loginRequest)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(CommonResp.error("登录凭据验证失败"));
            }

            // 获取或创建用户
            LoginStrategy.LoginResult loginResult = loginStrategy.getOrCreateUser(loginRequest);
            User user = loginResult.user();
            boolean isNewUser = loginResult.isNewUser();

            // 创建 DrinkupUserDetails
            DrinkupUserDetails userDetails = new DrinkupUserDetails(
                    user.getId(),
                    user.getUsername(),
                    "", // empty password as we're using other authentication methods
                    true,
                    user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .collect(Collectors.toList())
            );

            // 创建认证信息
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 清除旧会话并创建新会话
            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }
            HttpSession session = request.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    SecurityContextHolder.getContext());

            UserLoginResp loginResp = UserLoginResp.builder()
                    .message("登录成功")
                    .user(userMapper.toUserProfileResp(user))
                    .isNewUser(isNewUser)
                    .build();

            return ResponseEntity.ok(CommonResp.success(loginResp));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(CommonResp.error("登录失败: " + e.getMessage()));
        }
    }

    @LogRecord(
        type = UserEvent.USER,
        subType = UserEvent.BehaviorEvent.LOGOUT,
        bizNo = "null",
        success = "用户登出成功"
    )
    @Operation(summary = "用户登出", description = "用户登出并清除会话")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "登出成功")
    })
    @PostMapping("/logout")
    public ResponseEntity<CommonResp<LogoutResp>> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();

        LogoutResp logoutResp = LogoutResp.builder()
                .message("Logged out successfully")
                .status("success")
                .build();

        return ResponseEntity.ok(CommonResp.success(logoutResp));
    }
}