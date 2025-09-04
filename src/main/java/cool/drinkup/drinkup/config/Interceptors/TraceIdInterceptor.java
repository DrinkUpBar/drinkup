package cool.drinkup.drinkup.config.Interceptors;

import io.micrometer.tracing.Tracer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TraceIdInterceptor implements HandlerInterceptor {

    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private final Tracer tracer;

    public TraceIdInterceptor(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (tracer.currentSpan() != null) {
            String traceId = tracer.currentSpan().context().traceId();
            response.setHeader(TRACE_ID_HEADER, traceId);
        }
        return true;
    }
}
