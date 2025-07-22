package cool.drinkup.drinkup;

import com.mzt.logapi.starter.annotation.EnableLogRecord;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableRetry
@EnableLogRecord(tenant = "drinkup")
@EnableElasticsearchRepositories
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableScheduling
public class DrinkupApplication {

    public static void main(String[] args) {
        SpringApplication.run(DrinkupApplication.class, args);
    }
}
