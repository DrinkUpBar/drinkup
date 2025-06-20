package cool.drinkup.drinkup.user.internal.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.client.RestTemplate;

/**
 * Apple 登录相关的缓存和重试配置
 */
@Configuration
@EnableCaching
@EnableRetry
public class AppleCacheConfig {

    /**
     * 配置缓存管理器
     * 用于缓存Apple公钥等信息
     */
    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        // 设置缓存名称
        cacheManager.setCacheNames(java.util.List.of("applePublicKeys"));
        // 允许null值缓存
        cacheManager.setAllowNullValues(false);
        return cacheManager;
    }

    /**
     * 配置RestTemplate用于调用Apple API
     */
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        // 设置连接和读取超时
        restTemplate.getRequestFactory();

        return restTemplate;
    }
}