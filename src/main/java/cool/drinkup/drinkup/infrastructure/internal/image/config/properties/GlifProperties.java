package cool.drinkup.drinkup.infrastructure.internal.image.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "image.glif")
public class GlifProperties {
    private String apiUrl = "https://simple-api.glif.app";
    private String bearerToken;
    private String glifId;
    private String weights =
            "https://huggingface.co/alvdansen/softpasty-flux-dev/resolve/main/araminta_k_softpasty_diffusion_flux.safetensors";
}
