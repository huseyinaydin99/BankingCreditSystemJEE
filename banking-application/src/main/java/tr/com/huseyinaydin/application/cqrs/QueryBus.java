package tr.com.huseyinaydin.application.cqrs;

import tr.com.huseyinaydin.sharedkernel.messaging.IQuery;

public interface QueryBus {
    <TResult> TResult query(IQuery<TResult> query);
}
