package tr.com.huseyinaydin.sharedkernel.messaging;

public interface IQueryHandler<TQuery extends IQuery<TResult>, TResult> {
    TResult handle(TQuery query);
}
