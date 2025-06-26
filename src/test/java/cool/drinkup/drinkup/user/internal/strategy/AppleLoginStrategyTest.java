package cool.drinkup.drinkup.user.internal.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import cool.drinkup.drinkup.user.internal.controller.req.LoginRequest;
import cool.drinkup.drinkup.user.internal.service.AppleTokenService;
import cool.drinkup.drinkup.user.internal.service.UserService;
import cool.drinkup.drinkup.user.internal.service.strategy.impl.AppleLoginStrategy;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AppleLoginStrategyTest {

    @Mock
    private AppleTokenService appleTokenService;

    @Mock
    private UserService userService;

    @InjectMocks
    private AppleLoginStrategy appleLoginStrategy;

    @Test
    void testAppleLoginStrategy_getLoginType() {
        // When
        LoginRequest.LoginType loginType = appleLoginStrategy.getLoginType();

        // Then
        assertEquals(LoginRequest.LoginType.APPLE, loginType);
    }

    @Test
    void testAppleLoginStrategy_validateCredentials_validToken() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setLoginType(LoginRequest.LoginType.APPLE);
        request.setIdToken("valid.apple.token");

        Map<String, Object> userInfo = Map.of(
                "sub", "apple_user_123",
                "email", "user@example.com",
                "email_verified", true);

        when(appleTokenService.verifyIdToken(request.getIdToken())).thenReturn(userInfo);

        // When
        boolean result = appleLoginStrategy.validateCredentials(request);

        // Then
        assertTrue(result);
        verify(appleTokenService).verifyIdToken(request.getIdToken());
    }

    @Test
    void testAppleLoginStrategy_validateCredentials_invalidToken() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setLoginType(LoginRequest.LoginType.APPLE);
        request.setIdToken("invalid.apple.token");

        when(appleTokenService.verifyIdToken(request.getIdToken())).thenReturn(null);

        // When
        boolean result = appleLoginStrategy.validateCredentials(request);

        // Then
        assertFalse(result);
        verify(appleTokenService).verifyIdToken(request.getIdToken());
    }

    @Test
    void testAppleLoginStrategy_validateCredentials_emptyToken() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setLoginType(LoginRequest.LoginType.APPLE);
        request.setIdToken("");

        // When
        boolean result = appleLoginStrategy.validateCredentials(request);

        // Then
        assertFalse(result);
        verifyNoInteractions(appleTokenService);
    }

    @Test
    void testAppleLoginStrategy_validateCredentials_nullToken() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setLoginType(LoginRequest.LoginType.APPLE);
        request.setIdToken(null);

        // When
        boolean result = appleLoginStrategy.validateCredentials(request);

        // Then
        assertFalse(result);
        verifyNoInteractions(appleTokenService);
    }

    @Test
    void testAppleLoginStrategy_getUserIdentifier() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setIdToken("valid.apple.token");

        Map<String, Object> userInfo = Map.of(
                "sub", "apple_user_123",
                "email", "user@example.com");

        when(appleTokenService.verifyIdToken(anyString())).thenReturn(userInfo);

        // When
        String identifier = appleLoginStrategy.getUserIdentifier(request);

        // Then
        assertEquals("user@example.com", identifier);
    }

    @Test
    void testAppleLoginStrategy_getUserIdentifier_invalidToken() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setIdToken("invalid.apple.token");

        when(appleTokenService.verifyIdToken(anyString())).thenReturn(null);

        // When
        String identifier = appleLoginStrategy.getUserIdentifier(request);

        // Then
        assertEquals(null, identifier);
    }
}
