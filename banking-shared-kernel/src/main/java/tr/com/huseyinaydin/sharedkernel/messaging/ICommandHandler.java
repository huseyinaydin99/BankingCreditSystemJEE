package tr.com.huseyinaydin.sharedkernel.messaging;

public interface ICommandHandler<TCommand extends ICommand<TResult>, TResult> {
    TResult handle(TCommand command);
}
