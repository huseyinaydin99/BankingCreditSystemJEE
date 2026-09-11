package tr.com.huseyinaydin.application.pipeline.behavior;

import org.springframework.core.annotation.Order;
import tr.com.huseyinaydin.application.cqrs.IPipelineBehavior;
import tr.com.huseyinaydin.application.cqrs.PipelineDelegate;
import tr.com.huseyinaydin.application.ports.metrics.IMeterRegistry;

import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

@Component
@Order(2)
public class MetricsBehavior<TRequest, TResponse> implements IPipelineBehavior<TRequest, TResponse> {

    private final IMeterRegistry meterRegistry;

    public MetricsBehavior(IMeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public TResponse handle(TRequest request, PipelineDelegate<TResponse> next) {
        long startNs = System.nanoTime();
        boolean success = false;
        String requestType = request.getClass().getSimpleName();

        try {
            TResponse response = next.proceed();
            success = true;
            return response;
        } finally {
            long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
            
            meterRegistry.recordCommandDuration(requestType, durationMs, success);
            meterRegistry.incrementCounter("banking.command.count", "commandType", requestType, "status", success ? "success" : "failure");
        }
    }
}
