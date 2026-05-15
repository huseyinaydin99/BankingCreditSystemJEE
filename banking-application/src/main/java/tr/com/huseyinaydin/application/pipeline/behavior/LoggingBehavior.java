package tr.com.huseyinaydin.application.pipeline.behavior;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import tr.com.huseyinaydin.application.cqrs.IPipelineBehavior;
import tr.com.huseyinaydin.application.cqrs.PipelineDelegate;
import tr.com.huseyinaydin.application.pipeline.ICurrentUserService;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Order(3)
public class LoggingBehavior<TRequest, TResponse> implements IPipelineBehavior<TRequest, TResponse> {

    private static final Logger log = LoggerFactory.getLogger(LoggingBehavior.class);

    private static final Set<String> SENSITIVE_FIELDS =
            Set.of("password", "passwordhash", "passwordsalt", "hash", "salt", "token", "secret", "pin");

    private final ICurrentUserService currentUserService;

    public LoggingBehavior(ICurrentUserService currentUserService) {
        this.currentUserService = currentUserService;
    }

    @Override
    public TResponse handle(TRequest request, PipelineDelegate<TResponse> next) {
        String requestType = request.getClass().getSimpleName();
        String userId = resolveUserId();
        long startNs = System.nanoTime();

        log.info("Handling [{}] by [{}]", requestType, userId);

        try {
            TResponse response = next.proceed();
            long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
            log.info("[{}] tamamlandı — {}ms, kullanıcı: {}", requestType, elapsedMs, userId);
            return response;
        } catch (Exception ex) {
            long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
            log.error("[{}] HATA — {}ms, kullanıcı: {}, istek: {}, hata: {}",
                    requestType, elapsedMs, userId, toSafeString(request), ex.getMessage(), ex);
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

    private String toSafeString(Object obj) {
        if (obj == null) return "null";
        StringBuilder sb = new StringBuilder(obj.getClass().getSimpleName()).append("{");
        Field[] fields = obj.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            field.setAccessible(true);
            sb.append(field.getName()).append("=");
            if (SENSITIVE_FIELDS.contains(field.getName().toLowerCase())) {
                sb.append("***");
            } else {
                try {
                    sb.append(field.get(obj));
                } catch (IllegalAccessException e) {
                    sb.append("?");
                }
            }
            if (i < fields.length - 1) sb.append(", ");
        }
        return sb.append("}").toString();
    }
}
