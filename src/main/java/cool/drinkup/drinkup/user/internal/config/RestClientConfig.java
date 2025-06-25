package cool.drinkup.drinkup.user.internal.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * RestClient配置类
 * 提供RestClient的Bean实例
 */
@Configuration
public class RestClientConfig {

    /**
     * 创建RestClient Bean
     * 
     * @return RestClient实例
     */
    @Bean
    public RestClient restClient() {
        return RestClient.builder().build();
    }
}