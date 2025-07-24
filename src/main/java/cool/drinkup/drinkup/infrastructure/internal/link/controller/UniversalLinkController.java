package cool.drinkup.drinkup.infrastructure.internal.link.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Universal Link 控制器
 * 只提供 iOS Universal Links 配置文件
 */
@Slf4j
@RestController
@Tag(name = "Universal Link", description = "Universal Link配置接口")
public class UniversalLinkController {

    @Value("${drinkup.universal-link.ios-bundle-id:AW3YHD9A4T.com.drinkupbar.kaihe}")
    private String iosBundleId;

    @Value("${drinkup.universal-link.paths:/app/*}")
    private String[] paths;

    /**
     * 获取Apple App Site Association文件
     * iOS需要的配置文件，用于验证Universal Links
     */
    @Operation(summary = "获取Apple App Site Association", description = "获取iOS Universal Links配置文件")
    @GetMapping(value = "/.well-known/apple-app-site-association", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getAppleAppSiteAssociation() {
        log.info("获取Apple App Site Association文件");

        // 构建paths数组的JSON字符串
        StringBuilder pathsJson = new StringBuilder();
        for (int i = 0; i < paths.length; i++) {
            pathsJson.append("\"").append(paths[i]).append("\"");
            if (i < paths.length - 1) {
                pathsJson.append(", ");
            }
        }

        String association = String.format(
                """
                {
                    "applinks": {
                        "apps": [],
                        "details": [
                            {
                                "appID": "%s",
                                "paths": [ %s ]
                            }
                        ]
                    }
                }
                """,
                iosBundleId, pathsJson.toString());

        return ResponseEntity.ok().header("Content-Type", "application/json").body(association);
    }
}
