package cool.drinkup.drinkup.workflow.internal.util;

import jakarta.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.file.Files;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ContentTypeUtil {

    private final ServletContext servletContext;
    private final Tika tika = new Tika();

    public String detectMimeType(Resource resource) throws IOException {
        String mimeType = null;

        try (InputStream inputStream = resource.getInputStream()) {
            mimeType = tika.detect(inputStream);
        } catch (IOException e) {
            log.error("Failed to detect MIME type", e);
        }

        if (mimeType == null && servletContext != null) {
            mimeType = servletContext.getMimeType(resource.getFilename());
        }

        try (InputStream inputStream = resource.getInputStream()) {
            mimeType = URLConnection.guessContentTypeFromStream(inputStream);
        }

        // 3. 如果还是失败，尝试 Files.probeContentType
        if (mimeType == null) {
            try {
                mimeType = Files.probeContentType(resource.getFile().toPath());
            } catch (IOException | UnsupportedOperationException ignored) {
            }
        }

        return mimeType != null ? mimeType : "application/octet-stream";
    }
}
