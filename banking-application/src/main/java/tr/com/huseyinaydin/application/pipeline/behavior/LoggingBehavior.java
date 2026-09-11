package tr.com.huseyinaydin.application.pipeline.behavior;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import tr.com.huseyinaydin.application.cqrs.IPipelineBehavior;
import tr.com.huseyinaydin.application.cqrs.PipelineDelegate;
import tr.com.huseyinaydin.application.logging.MaskingSerializer;
import tr.com.huseyinaydin.application.pipeline.ICurrentUserService;

import java.util.concurrent.TimeUnit;


@Order(3)
public class LoggingBehavior<TRequest, TResponse> implements IPipelineBehavior<TRequest, TResponse> {

    private static final Logger log = LoggerFactory.getLogger(LoggingBehavior.class);

    private static final String MDC_CORRELATION_ID = "correlationId";

    private final ICurrentUserService currentUserService;
    private final Tracer tracer;

    public LoggingBehavior(ICurrentUserService currentUserService, Tracer tracer) {
        this.currentUserService = currentUserService;
        this.tracer = tracer;
    }

    @Override
    public TResponse handle(TRequest request, PipelineDelegate<TResponse> next) {
        String correlationId = MDC.get(MDC_CORRELATION_ID);
        String userId = resolveUserId();
        String commandType = request.getClass().getSimpleName();
        long startNs = System.nanoTime();
        
        Span currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            MDC.put("traceId", currentSpan.context().traceId());
            MDC.put("spanId", currentSpan.context().spanId());
        }

        try {
            TResponse response = next.proceed();
            long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
            log.atInfo()
                    .addKeyValue("correlationId", correlationId)
                    .addKeyValue("userId", userId)
                    .addKeyValue("commandType", commandType)
                    .addKeyValue("durationMs", elapsedMs)
                    .addKeyValue("success", true)
                    .log("command handled");
            return response;
        } catch (Exception ex) {
            long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
            log.atError()
                    .addKeyValue("correlationId", correlationId)
                    .addKeyValue("userId", userId)
                    .addKeyValue("commandType", commandType)
                    .addKeyValue("durationMs", elapsedMs)
                    .addKeyValue("success", false)
                    .addKeyValue("errorType", ex.getClass().getSimpleName())
                    .addKeyValue("request", MaskingSerializer.serialize(request))
                    .setCause(ex)
                    .log("command failed");
            throw ex;
        } finally {
            MDC.remove("traceId");
            MDC.remove("spanId");
        }
    }

    private String resolveUserId() {
        if (currentUserService == null) return "anonymous";
        try {
            return currentUserService.isAuthenticated()
                    ? currentUserService.getCurrentUserId()
                    : "anonymous";
        } catch (Exception e) {
            return "unknown";
        }
    }
}
