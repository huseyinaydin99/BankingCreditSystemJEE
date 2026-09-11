package tr.com.huseyinaydin.application.cqrs;

import java.util.List;

public final class CommandPipeline {

    private CommandPipeline() {}

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <TRequest, TResult> TResult execute(
            TRequest request,
            List<IPipelineBehavior<?, ?>> behaviors,
            PipelineDelegate<TResult> terminal) {
        PipelineDelegate<TResult> chain = terminal;

        for (int i = behaviors.size() - 1; i >= 0; i--) {
            IPipelineBehavior behavior = behaviors.get(i);
            PipelineDelegate<TResult> next = chain;
            chain = () -> (TResult) behavior.handle(request, next);
        }
        return chain.proceed();
    }

}
