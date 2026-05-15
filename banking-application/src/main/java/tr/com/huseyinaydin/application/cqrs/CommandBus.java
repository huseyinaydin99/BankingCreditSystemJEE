package tr.com.huseyinaydin.application.cqrs;

import tr.com.huseyinaydin.sharedkernel.messaging.ICommand;

import java.util.concurrent.CompletableFuture;

public interface CommandBus {
    <TResult> TResult send(ICommand<TResult> command);
    <TResult> CompletableFuture<TResult> sendAsync(ICommand<TResult> command);
}
