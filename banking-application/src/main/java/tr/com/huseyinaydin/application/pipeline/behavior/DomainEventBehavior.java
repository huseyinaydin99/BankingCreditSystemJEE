package tr.com.huseyinaydin.application.pipeline.behavior;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import tr.com.huseyinaydin.application.cqrs.IPipelineBehavior;
import tr.com.huseyinaydin.application.cqrs.PipelineDelegate;
import tr.com.huseyinaydin.application.ports.IEventPublisher;
import tr.com.huseyinaydin.sharedkernel.events.DomainEvent;

import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

@Order(7)
public class DomainEventBehavior<TRequest, TResponse> implements IPipelineBehavior<TRequest, TResponse> {

    private static final Logger log = LoggerFactory.getLogger(DomainEventBehavior.class);
    private final IEventPublisher eventPublisher;

    public DomainEventBehavior(IEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public TResponse handle(TRequest request, PipelineDelegate<TResponse> next) {
        TResponse response = next.proceed();
        
        if (response != null) {
            publishEventsFromObject(response);
        }
        
        return response;
    }

    private void publishEventsFromObject(Object obj) {
        if (obj == null) return;
        
        try {
            // obj directly has pullDomainEvents method
            Method pullMethod = null;
            try {
                pullMethod = obj.getClass().getMethod("pullDomainEvents");
            } catch (NoSuchMethodException e) {
                // Ignore
            }

            if (pullMethod != null) {
                @SuppressWarnings("unchecked")
                List<DomainEvent> events = (List<DomainEvent>) pullMethod.invoke(obj);
                if (events != null) {
                    for (DomainEvent event : events) {
                        log.debug("Publishing event: {}", event.getClass().getSimpleName());
                        eventPublisher.publish(event);
                    }
                }
            }

            // Check fields for aggregate roots
            Field[] fields = obj.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                Object fieldValue = field.get(obj);
                if (fieldValue != null) {
                    try {
                        Method fieldPullMethod = fieldValue.getClass().getMethod("pullDomainEvents");
                        @SuppressWarnings("unchecked")
                        List<DomainEvent> events = (List<DomainEvent>) fieldPullMethod.invoke(fieldValue);
                        if (events != null) {
                            for (DomainEvent event : events) {
                                log.debug("Publishing event: {}", event.getClass().getSimpleName());
                                eventPublisher.publish(event);
                            }
                        }
                    } catch (NoSuchMethodException e) {
                        // ignore
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error publishing domain events from result", e);
        }
    }
}
