package cool.drinkup.drinkup.user.internal.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import java.time.Duration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
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
     * 配置基于Redis的缓存管理器
     * 用于缓存Apple公钥等信息
     */
    @Bean("appleCacheManager")
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory, ObjectMapper objectMapper) {
        // 为缓存创建专门的ObjectMapper副本，确保类型信息正确保存
        ObjectMapper cacheObjectMapper = objectMapper.copy();
        cacheObjectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

        // 使用配置了类型信息的Jackson序列化器
        GenericJackson2JsonRedisSerializer jackson2JsonRedisSerializer =
                new GenericJackson2JsonRedisSerializer(cacheObjectMapper);

        // 配置缓存序列化方式
        RedisCacheConfiguration cacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                // 设置缓存过期时间为24小时（Apple公钥通常24小时更新一次）
                .entryTtl(Duration.ofHours(24))
                // 禁止缓存null值
                .disableCachingNullValues()
                // 设置key序列化方式
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                // 设置value序列化方式，使用带类型信息的序列化器
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(jackson2JsonRedisSerializer))
                // 设置缓存名称前缀
                .prefixCacheNameWith("apple:cache:");

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(cacheConfiguration)
                // 配置具体的缓存名称和过期时间
                .withCacheConfiguration("applePublicKeys", cacheConfiguration)
                // 允许运行时创建缓存
                .allowCreateOnMissingCache(true)
                .build();
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
