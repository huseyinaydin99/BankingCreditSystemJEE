package tr.com.huseyinaydin.infrastructure.config;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import jakarta.persistence.OptimisticLockException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessResourceFailureException;

import java.time.Duration;

@Configuration
public class ResilienceConfig {

    @Bean
    public RetryRegistry retryRegistry() {
        return RetryRegistry.ofDefaults();
    }

    @Bean
    public Retry jpaRetry(RetryRegistry retryRegistry) {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(500))
                .intervalFunction(io.github.resilience4j.core.IntervalFunction.ofExponentialBackoff(Duration.ofMillis(500), 2.0))
                .retryExceptions(OptimisticLockException.class, DataAccessResourceFailureException.class)
                .build();
        return retryRegistry.retry("jpaRetry", config);
    }
}
