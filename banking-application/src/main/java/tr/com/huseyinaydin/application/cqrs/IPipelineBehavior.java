package tr.com.huseyinaydin.application.cqrs;

import org.springframework.core.annotation.Order;


public interface IPipelineBehavior<TRequest, TResponse> {
    TResponse handle(TRequest request, PipelineDelegate<TResponse> next);
}
