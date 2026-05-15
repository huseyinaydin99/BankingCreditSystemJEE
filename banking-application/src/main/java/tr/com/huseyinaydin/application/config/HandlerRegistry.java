package tr.com.huseyinaydin.application.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.sharedkernel.messaging.ICommandHandler;
import tr.com.huseyinaydin.sharedkernel.messaging.IQueryHandler;

import java.util.Map;

@Component
public class HandlerRegistry implements SmartInitializingSingleton {

    private static final Logger log = LoggerFactory.getLogger(HandlerRegistry.class);

    private final ApplicationContext context;

    public HandlerRegistry(ApplicationContext context) {
        this.context = context;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void afterSingletonsInstantiated() {
        Map<String, ICommandHandler> commandHandlers = context.getBeansOfType(ICommandHandler.class);
        Map<String, IQueryHandler> queryHandlers = context.getBeansOfType(IQueryHandler.class);

        log.info("CQRS altyapısı hazır — komut handler: {}, sorgu handler: {}",
                commandHandlers.size(), queryHandlers.size());

        if (commandHandlers.isEmpty()) {
            log.warn("Hiç ICommandHandler bulunamadı. Handler sınıfları @Component ile işaretlenmiş olmalıdır.");
        }
        if (queryHandlers.isEmpty()) {
            log.warn("Hiç IQueryHandler bulunamadı. Handler sınıfları @Component ile işaretlenmiş olmalıdır.");
        }

        commandHandlers.forEach((beanName, handler) -> {
            ResolvableType type = ResolvableType.forClass(handler.getClass()).as(ICommandHandler.class);
            Class<?> commandType = type.getGeneric(0).resolve();
            log.debug("  [CMD] {} → {}",
                    commandType != null ? commandType.getSimpleName() : "?",
                    handler.getClass().getSimpleName());
        });

        queryHandlers.forEach((beanName, handler) -> {
            ResolvableType type = ResolvableType.forClass(handler.getClass()).as(IQueryHandler.class);
            Class<?> queryType = type.getGeneric(0).resolve();
            log.debug("  [QRY] {} → {}",
                    queryType != null ? queryType.getSimpleName() : "?",
                    handler.getClass().getSimpleName());
        });
    }
}
