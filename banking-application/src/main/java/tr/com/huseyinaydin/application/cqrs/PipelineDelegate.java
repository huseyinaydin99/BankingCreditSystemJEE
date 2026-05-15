package tr.com.huseyinaydin.application.cqrs;

@FunctionalInterface
public interface PipelineDelegate<TResponse> {
    TResponse proceed();
}
