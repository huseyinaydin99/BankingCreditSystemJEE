package tr.com.huseyinaydin.application.pipeline.behavior;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import tr.com.huseyinaydin.application.cqrs.IPipelineBehavior;
import tr.com.huseyinaydin.application.cqrs.PipelineDelegate;
import tr.com.huseyinaydin.application.logging.MaskingSerializer;
import tr.com.huseyinaydin.application.pipeline.ICurrentUserService;

import java.util.concurrent.TimeUnit;

/**
 * Her command/request için yapılandırılmış (structured) log üretir. Alanlar SLF4J 2.0
 * key-value API'siyle ({@code addKeyValue}) eklenir; LogstashEncoder bunları JSON alanı
 * olarak (tip korunarak) render eder. correlationId, MDC'den ({@code CorrelationIdFilter}
 * tarafından set edilir) okunur.
 *
 * Alanlar: correlationId, userId, commandType, durationMs, success, errorType (hata varsa).
 * Hata durumunda maskelenmiş istek payload'ı da {@code request} alanına yazılır
 * ({@link MaskingSerializer} ile gizli alanlar gizlenir).
 */
@Order(3)
public class LoggingBehavior<TRequest, TResponse> implements IPipelineBehavior<TRequest, TResponse> {

    private static final Logger log = LoggerFactory.getLogger(LoggingBehavior.class);

    private static final String MDC_CORRELATION_ID = "correlationId";

    private final ICurrentUserService currentUserService;

    public LoggingBehavior(ICurrentUserService currentUserService) {
        this.currentUserService = currentUserService;
    }

    @Override
    public TResponse handle(TRequest request, PipelineDelegate<TResponse> next) {
        String correlationId = MDC.get(MDC_CORRELATION_ID);
        String userId = resolveUserId();
        String commandType = request.getClass().getSimpleName();
        long startNs = System.nanoTime();

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
