package tr.com.huseyinaydin.application.cqrs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import tr.com.huseyinaydin.application.exception.HandlerNotFoundException;
import tr.com.huseyinaydin.sharedkernel.messaging.ICommand;
import tr.com.huseyinaydin.sharedkernel.messaging.ICommandHandler;
import tr.com.huseyinaydin.sharedkernel.messaging.IQuery;
import tr.com.huseyinaydin.sharedkernel.messaging.IQueryHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

public class SpringMediator implements Mediator {

    private static final Logger log = LoggerFactory.getLogger(SpringMediator.class);

    private final ApplicationContext context;
    private final Executor asyncExecutor;

    private final Map<Class<?>, ICommandHandler<?, ?>> commandHandlerCache = new ConcurrentHashMap<>();
    private final Map<Class<?>, IQueryHandler<?, ?>> queryHandlerCache = new ConcurrentHashMap<>();

    private volatile List<IPipelineBehavior<?, ?>> cachedBehaviors;

    public SpringMediator(ApplicationContext context, Executor asyncExecutor) {
        this.context = context;
        this.asyncExecutor = asyncExecutor;
    }

    @Override
    public <TResult> TResult send(ICommand<TResult> command) {
        ICommandHandler<ICommand<TResult>, TResult> handler = resolveCommandHandler(command);
        List<IPipelineBehavior<?, ?>> behaviors = getBehaviors();
        return CommandPipeline.execute(command, behaviors, () -> handler.handle(command));
    }

    @Override
    public <TResult> CompletableFuture<TResult> sendAsync(ICommand<TResult> command) {
        return CompletableFuture.supplyAsync(() -> send(command), asyncExecutor);
    }

    @Override
    public <TResult> TResult query(IQuery<TResult> query) {
        IQueryHandler<IQuery<TResult>, TResult> handler = resolveQueryHandler(query);
        return handler.handle(query);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private <TResult> ICommandHandler<ICommand<TResult>, TResult> resolveCommandHandler(ICommand<TResult> command) {
        Class<?> commandClass = command.getClass();

        ICommandHandler<?, ?> handler = commandHandlerCache.get(commandClass);
        if (handler != null) {
            return (ICommandHandler<ICommand<TResult>, TResult>) handler;
        }

        Map<String, ICommandHandler> allHandlers = context.getBeansOfType(ICommandHandler.class);
        for (Map.Entry<String, ICommandHandler> entry : allHandlers.entrySet()) {
            ResolvableType handlerType = ResolvableType.forClass(entry.getValue().getClass())
                    .as(ICommandHandler.class);
            Class<?> resolvedCommandType = handlerType.getGeneric(0).resolve();

            if (commandClass.equals(resolvedCommandType)) {
                commandHandlerCache.putIfAbsent(commandClass, entry.getValue());
                log.debug("Handler çözümlendi: {} → {}", commandClass.getSimpleName(), entry.getKey());
                return (ICommandHandler<ICommand<TResult>, TResult>) commandHandlerCache.get(commandClass);
            }
        }

        throw new HandlerNotFoundException(commandClass);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private <TResult> IQueryHandler<IQuery<TResult>, TResult> resolveQueryHandler(IQuery<TResult> query) {
        Class<?> queryClass = query.getClass();

        IQueryHandler<?, ?> handler = queryHandlerCache.get(queryClass);
        if (handler != null) {
            return (IQueryHandler<IQuery<TResult>, TResult>) handler;
        }

        Map<String, IQueryHandler> allHandlers = context.getBeansOfType(IQueryHandler.class);
        for (Map.Entry<String, IQueryHandler> entry : allHandlers.entrySet()) {
            ResolvableType handlerType = ResolvableType.forClass(entry.getValue().getClass())
                    .as(IQueryHandler.class);
            Class<?> resolvedQueryType = handlerType.getGeneric(0).resolve();

            if (queryClass.equals(resolvedQueryType)) {
                queryHandlerCache.putIfAbsent(queryClass, entry.getValue());
                log.debug("Query handler çözümlendi: {} → {}", queryClass.getSimpleName(), entry.getKey());
                return (IQueryHandler<IQuery<TResult>, TResult>) queryHandlerCache.get(queryClass);
            }
        }

        throw new HandlerNotFoundException(queryClass);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private List<IPipelineBehavior<?, ?>> getBehaviors() {
        if (cachedBehaviors == null) {
            synchronized (this) {
                if (cachedBehaviors == null) {
                    Map<String, IPipelineBehavior> behaviorBeans = context.getBeansOfType(IPipelineBehavior.class);
                    List rawList = new ArrayList(behaviorBeans.values());
                    AnnotationAwareOrderComparator.sort(rawList);
                    cachedBehaviors = rawList;
                    log.debug("Pipeline behavior'lar yüklendi: {}", rawList.size());
                }
            }
        }
        return cachedBehaviors;
    }
}
