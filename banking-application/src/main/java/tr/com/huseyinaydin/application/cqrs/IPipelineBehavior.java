package tr.com.huseyinaydin.application.cqrs;

import org.springframework.core.annotation.Order;

/**
 * Pipeline interceptor. Spring @Order ile öncelik sırası belirlenir (düşük = önce çalışır).
 * @see Order
 */
public interface IPipelineBehavior<TRequest, TResponse> {
    TResponse handle(TRequest request, PipelineDelegate<TResponse> next);
}
