package tr.com.huseyinaydin.application.pipeline.behavior;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.springframework.core.annotation.Order;
import tr.com.huseyinaydin.application.cqrs.IPipelineBehavior;
import tr.com.huseyinaydin.application.cqrs.PipelineDelegate;

@Order(3)
public class TracingBehavior<TRequest, TResponse> implements IPipelineBehavior<TRequest, TResponse> {

    private final Tracer tracer;

    public TracingBehavior(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public TResponse handle(TRequest request, PipelineDelegate<TResponse> next) {
        if (tracer == null) {
            return next.proceed();
        }

        String handlerName = request.getClass().getSimpleName() + "Handler";
        Span span = tracer.nextSpan().name(handlerName);

        try (Tracer.SpanInScope ws = tracer.withSpan(span.start())) {
            return next.proceed();
        } finally {
            span.end();
        }
    }
}
