package tr.com.huseyinaydin.infrastructure.metrics;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.event.CircuitBreakerOnErrorEvent;
import io.github.resilience4j.circuitbreaker.event.CircuitBreakerOnStateTransitionEvent;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.event.RetryOnErrorEvent;
import io.github.resilience4j.retry.event.RetryOnRetryEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.InitializingBean;

@Component
public class ResilienceEventListener implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(ResilienceEventListener.class);

    private final CircuitBreaker circuitBreaker;
    private final Retry retry;

    public ResilienceEventListener(
            @org.springframework.beans.factory.annotation.Autowired(required = false) CircuitBreaker circuitBreaker,
            @org.springframework.beans.factory.annotation.Autowired(required = false) Retry retry) {
        this.circuitBreaker = circuitBreaker;
        this.retry = retry;
    }

    @Override
    public void afterPropertiesSet() {
        if (circuitBreaker != null) {
            circuitBreaker.getEventPublisher()
                .onError(this::onCircuitBreakerError)
                .onStateTransition(this::onStateTransition);
        }

        if (retry != null) {
            retry.getEventPublisher()
                .onRetry(this::onRetry)
                .onError(this::onRetryError);
        }
    }

    private void onCircuitBreakerError(CircuitBreakerOnErrorEvent event) {
        log.warn("CircuitBreaker '{}' error: {}", event.getCircuitBreakerName(), event.getThrowable().getMessage());
    }

    private void onStateTransition(CircuitBreakerOnStateTransitionEvent event) {
        log.warn("CircuitBreaker '{}' state transition from {} to {}", 
                event.getCircuitBreakerName(), 
                event.getStateTransition().getFromState(), 
                event.getStateTransition().getToState());
    }

    private void onRetry(RetryOnRetryEvent event) {
        log.warn("Retry '{}' attempt {}/{} after error: {}", 
                event.getName(), 
                event.getNumberOfRetryAttempts(), 
                3, 
                event.getLastThrowable().getMessage());
    }

    private void onRetryError(RetryOnErrorEvent event) {
        log.warn("Retry '{}' failed after {} attempts. Last error: {}", 
                event.getName(), 
                event.getNumberOfRetryAttempts(), 
                event.getLastThrowable().getMessage());
    }
}
