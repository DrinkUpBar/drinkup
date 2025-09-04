package cool.drinkup.drinkup.user.internal.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import cool.drinkup.drinkup.infrastructure.spi.sms.SmsSender;
import cool.drinkup.drinkup.user.internal.controller.req.LoginRequest;
import cool.drinkup.drinkup.user.internal.mapper.UserMapper;
import cool.drinkup.drinkup.user.internal.service.UserService;
import cool.drinkup.drinkup.user.internal.service.strategy.impl.SmsLoginStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoginStrategyTest {

    @Mock
    private SmsSender smsSender;

    @Mock
    private UserService userService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private SmsLoginStrategy smsLoginStrategy;

    @Test
    void testSmsLoginStrategy_getLoginType() {
        // When
        LoginRequest.LoginType loginType = smsLoginStrategy.getLoginType();

        // Then
        assertEquals(LoginRequest.LoginType.SMS, loginType);
    }

    @Test
    void testSmsLoginStrategy_validateCredentials_testUser() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setLoginType(LoginRequest.LoginType.SMS);
        request.setPhone("13800138000");
        request.setVerificationCode("250528");

        // When
        boolean result = smsLoginStrategy.validateCredentials(request);

        // Then
        assertTrue(result);
        verifyNoInteractions(smsSender); // 测试用户不应该调用 SMS 验证
    }

    @Test
    void testSmsLoginStrategy_validateCredentials_regularUser() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setLoginType(LoginRequest.LoginType.SMS);
        request.setPhone("13800138001");
        request.setVerificationCode("123456");

        when(smsSender.verifySms(request.getPhone(), request.getVerificationCode()))
                .thenReturn(true);

        // When
        boolean result = smsLoginStrategy.validateCredentials(request);

        // Then
        assertTrue(result);
        verify(smsSender).verifySms(request.getPhone(), request.getVerificationCode());
    }

    @Test
    void testSmsLoginStrategy_validateCredentials_invalidCredentials() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setLoginType(LoginRequest.LoginType.SMS);
        request.setPhone("13800138001");
        request.setVerificationCode("000000");

        when(smsSender.verifySms(request.getPhone(), request.getVerificationCode()))
                .thenReturn(false);

        // When
        boolean result = smsLoginStrategy.validateCredentials(request);

        // Then
        assertFalse(result);
        verify(smsSender).verifySms(request.getPhone(), request.getVerificationCode());
    }

    @Test
    void testSmsLoginStrategy_validateCredentials_emptyPhone() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setLoginType(LoginRequest.LoginType.SMS);
        request.setPhone("");
        request.setVerificationCode("123456");

        // When
        boolean result = smsLoginStrategy.validateCredentials(request);

        // Then
        assertFalse(result);
        verifyNoInteractions(smsSender);
    }

    @Test
    void testSmsLoginStrategy_validateCredentials_emptyVerificationCode() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setLoginType(LoginRequest.LoginType.SMS);
        request.setPhone("13800138001");
        request.setVerificationCode("");

        // When
        boolean result = smsLoginStrategy.validateCredentials(request);

        // Then
        assertFalse(result);
        verifyNoInteractions(smsSender);
    }

    @Test
    void testSmsLoginStrategy_getUserIdentifier() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setPhone("13800138000");

        // When
        String identifier = smsLoginStrategy.getUserIdentifier(request);

        // Then
        assertEquals("13800138000", identifier);
    }
}
