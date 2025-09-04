package cool.drinkup.drinkup.infrastructure.internal.image.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "drinkup.image.proxy")
public class ImgProxyProperties {
    private String url = "https://imgproxy.fjhdream.lol/";

    private String key = "2d70014eafede06dd12db052bf42d8af28ab34e9a1339802fafbd5c0bc2f4d7a";

    private String salt = "41c5445e60510ff01b476108f2ddc6a9f22b7a550b9d03bfd08f37df07973d23";

    private String param = "rs:fit:1600:1600/q:80";
}
