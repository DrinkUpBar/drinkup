package cool.drinkup.drinkup.user.internal.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Apple OAuth 配置测试
 */
class AppleOAuthConfigTest {

    private AppleOAuthConfig config;

    @BeforeEach
    void setUp() {
        config = new AppleOAuthConfig();
    }

    @Test
    void testGetAllClientIds_WithClientIdsList() {
        // Given
        List<String> clientIds =
                Arrays.asList("com.drinkupbar.kaihe", "com.drinkupbar.kaihe.dev", "com.drinkupbar.kaihe.web");
        config.setClientIds(clientIds);
        config.setClientId("com.drinkupbar.single"); // 这个应该被忽略

        // When
        List<String> result = config.getAllClientIds();

        // Then
        assertEquals(3, result.size());
        assertTrue(result.contains("com.drinkupbar.kaihe"));
        assertTrue(result.contains("com.drinkupbar.kaihe.dev"));
        assertTrue(result.contains("com.drinkupbar.kaihe.web"));
        assertFalse(result.contains("com.drinkupbar.single"));
    }

    @Test
    void testGetAllClientIds_WithSingleClientId() {
        // Given
        config.setClientId("com.drinkupbar.single");
        // clientIds 为空

        // When
        List<String> result = config.getAllClientIds();

        // Then
        assertEquals(1, result.size());
        assertEquals("com.drinkupbar.single", result.get(0));
    }

    @Test
    void testGetAllClientIds_WithEmptyClientIdsList() {
        // Given
        config.setClientIds(Arrays.asList()); // 空列表
        config.setClientId("com.drinkupbar.single");

        // When
        List<String> result = config.getAllClientIds();

        // Then
        assertEquals(1, result.size());
        assertEquals("com.drinkupbar.single", result.get(0));
    }

    @Test
    void testGetAllClientIds_WithNoConfiguration() {
        // Given
        // clientIds 和 clientId 都为空/null

        // When
        List<String> result = config.getAllClientIds();

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetAllClientIds_WithEmptyClientId() {
        // Given
        config.setClientId(""); // 空字符串
        config.setClientId("   "); // 仅空格

        // When
        List<String> result = config.getAllClientIds();

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void testIsValidClientId_ValidId() {
        // Given
        List<String> clientIds = Arrays.asList("com.drinkupbar.kaihe", "com.drinkupbar.kaihe.dev");
        config.setClientIds(clientIds);

        // When & Then
        assertTrue(config.isValidClientId("com.drinkupbar.kaihe"));
        assertTrue(config.isValidClientId("com.drinkupbar.kaihe.dev"));
    }

    @Test
    void testIsValidClientId_InvalidId() {
        // Given
        List<String> clientIds = Arrays.asList("com.drinkupbar.kaihe", "com.drinkupbar.kaihe.dev");
        config.setClientIds(clientIds);

        // When & Then
        assertFalse(config.isValidClientId("com.invalid.app"));
        assertFalse(config.isValidClientId(null));
        assertFalse(config.isValidClientId(""));
        assertFalse(config.isValidClientId("   "));
    }

    @Test
    void testIsValidClientId_WithSingleClientId() {
        // Given
        config.setClientId("com.drinkupbar.single");

        // When & Then
        assertTrue(config.isValidClientId("com.drinkupbar.single"));
        assertFalse(config.isValidClientId("com.invalid.app"));
    }

    @Test
    void testGetPrimaryClientId_WithClientIdsList() {
        // Given
        List<String> clientIds =
                Arrays.asList("com.drinkupbar.kaihe", "com.drinkupbar.kaihe.dev", "com.drinkupbar.kaihe.web");
        config.setClientIds(clientIds);

        // When
        String result = config.getPrimaryClientId();

        // Then
        assertEquals("com.drinkupbar.kaihe", result);
    }

    @Test
    void testGetPrimaryClientId_WithSingleClientId() {
        // Given
        config.setClientId("com.drinkupbar.single");

        // When
        String result = config.getPrimaryClientId();

        // Then
        assertEquals("com.drinkupbar.single", result);
    }

    @Test
    void testGetPrimaryClientId_WithNoConfiguration() {
        // Given
        // 没有配置任何客户端ID

        // When
        String result = config.getPrimaryClientId();

        // Then
        assertNull(result);
    }

    @Test
    void testGetAllClientIds_ReturnsNewList() {
        // Given
        List<String> originalList = Arrays.asList("com.drinkupbar.kaihe");
        config.setClientIds(originalList);

        // When
        List<String> result1 = config.getAllClientIds();
        List<String> result2 = config.getAllClientIds();

        // Then
        assertEquals(result1, result2);
        // 验证返回的是新的列表实例，不是同一个引用
        assertFalse(result1 == result2);
        assertFalse(result1 == originalList);
    }

    @Test
    void testBackwardCompatibility() {
        // Given - 模拟旧的配置方式
        config.setClientId("com.drinkupbar.legacy");

        // When
        List<String> allIds = config.getAllClientIds();
        String primaryId = config.getPrimaryClientId();
        boolean isValid = config.isValidClientId("com.drinkupbar.legacy");

        // Then
        assertEquals(1, allIds.size());
        assertEquals("com.drinkupbar.legacy", allIds.get(0));
        assertEquals("com.drinkupbar.legacy", primaryId);
        assertTrue(isValid);
    }
}
