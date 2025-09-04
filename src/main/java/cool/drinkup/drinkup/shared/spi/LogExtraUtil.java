package cool.drinkup.drinkup.shared.spi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.micrometer.tracing.CurrentTraceContext;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogExtraUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final Tracer tracer;

    public String getLogExtra(Object obj) {
        try {
            ObjectNode logNode = objectMapper.createObjectNode();

            CurrentTraceContext context = tracer.currentTraceContext();
            String traceId = null;
            if (context != null) {
                traceId = context.context().traceId();
            }
            // 添加TraceId字段
            if (traceId != null) {
                logNode.put("traceId", traceId);
            }

            // 添加原始对象数据
            if (obj != null) {
                ObjectNode objNode = objectMapper.valueToTree(obj);
                logNode.setAll(objNode);
            }

            return objectMapper.writeValueAsString(logNode);
        } catch (Exception e) {
            return null;
        }
    }
}
