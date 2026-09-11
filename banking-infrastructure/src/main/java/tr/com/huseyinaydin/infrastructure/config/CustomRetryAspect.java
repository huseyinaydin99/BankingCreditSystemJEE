package tr.com.huseyinaydin.infrastructure.config;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class CustomRetryAspect {

    private final RetryRegistry retryRegistry;

    public CustomRetryAspect(RetryRegistry retryRegistry) {
        this.retryRegistry = retryRegistry;
    }

    @Around("@annotation(retryAnnotation)")
    public Object retry(ProceedingJoinPoint joinPoint, io.github.resilience4j.retry.annotation.Retry retryAnnotation) throws Throwable {
        Retry retry = retryRegistry.retry(retryAnnotation.name());
        return retry.executeCheckedSupplier(joinPoint::proceed);
    }
}
