package cool.drinkup.drinkup.user.internal.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;

import com.fasterxml.jackson.databind.ObjectMapper;
import cool.drinkup.drinkup.user.internal.config.AppleOAuthConfig;
import java.security.PublicKey;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Apple Token 验证服务测试
 */
@ExtendWith(MockitoExtension.class)
class AppleTokenServiceTest {

    @Mock
    private AppleOAuthConfig appleOAuthConfig;

    @Mock
    private ApplePublicKeyService applePublicKeyService;

    @Mock
    private PublicKey publicKey;

    private ObjectMapper objectMapper;
    private AppleTokenService appleTokenService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        appleTokenService = new AppleTokenService(appleOAuthConfig, applePublicKeyService, objectMapper);

        // 使用 lenient() 来避免不必要的 stubbing 错误
        lenient().when(appleOAuthConfig.getClientId()).thenReturn("com.example.app");
    }

    @Test
    void testIsValidTokenFormat_ValidToken() {
        // 三部分的有效JWT格式
        String validToken = "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.signature";
        assertTrue(appleTokenService.isValidTokenFormat(validToken));
    }

    @Test
    void testIsValidTokenFormat_InvalidToken() {
        // 无效格式的token
        assertFalse(appleTokenService.isValidTokenFormat(null));
        assertFalse(appleTokenService.isValidTokenFormat(""));
        assertFalse(appleTokenService.isValidTokenFormat("invalid.token"));
        assertFalse(appleTokenService.isValidTokenFormat("too.many.parts.here"));
    }

    @Test
    void testVerifyIdToken_InvalidFormat() {
        String invalidToken = "invalid.token";
        Map<String, Object> result = appleTokenService.verifyIdToken(invalidToken);
        assertNull(result);
    }

    @Test
    void testVerifyIdToken_NullToken() {
        Map<String, Object> result = appleTokenService.verifyIdToken(null);
        assertNull(result);
    }

    @Test
    void testVerifyIdToken_EmptyToken() {
        Map<String, Object> result = appleTokenService.verifyIdToken("");
        assertNull(result);
    }

    @Test
    void testClearPublicKeyCache() {
        // 测试清除缓存方法不会抛出异常
        assertDoesNotThrow(() -> appleTokenService.clearPublicKeyCache());
    }

    /**
     * 注意：完整的JWT验证测试需要真实的Apple JWT Token或者更复杂的mock设置
     * 这里只测试了基本的格式验证和边界条件
     * 在实际项目中，可以使用Apple的测试环境或者模拟JWT来进行更全面的测试
     */
}
