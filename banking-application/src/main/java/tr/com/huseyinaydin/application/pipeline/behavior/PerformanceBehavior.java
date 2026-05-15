package tr.com.huseyinaydin.application.pipeline.behavior;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import tr.com.huseyinaydin.application.cqrs.IPipelineBehavior;
import tr.com.huseyinaydin.application.cqrs.PipelineDelegate;

import java.util.concurrent.TimeUnit;

@Order(4)
public class PerformanceBehavior<TRequest, TResponse> implements IPipelineBehavior<TRequest, TResponse> {

    private static final Logger log = LoggerFactory.getLogger(PerformanceBehavior.class);

    private static final long WARN_THRESHOLD_MS  = 500L;
    private static final long ERROR_THRESHOLD_MS = 2000L;

    @Override
    public TResponse handle(TRequest request, PipelineDelegate<TResponse> next) {
        long startNs = System.nanoTime();
        try {
            return next.proceed();
        } finally {
            long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
            String requestType = request.getClass().getSimpleName();

            if (elapsedMs >= ERROR_THRESHOLD_MS) {
                log.error("YAVAŞ İSTEK [{}] — {}ms (eşik: {}ms). Performans incelemesi gereklidir.",
                        requestType, elapsedMs, ERROR_THRESHOLD_MS);
            } else if (elapsedMs >= WARN_THRESHOLD_MS) {
                log.warn("Yavaş istek uyarısı [{}] — {}ms (eşik: {}ms)",
                        requestType, elapsedMs, WARN_THRESHOLD_MS);
            }
        }
    }
}
