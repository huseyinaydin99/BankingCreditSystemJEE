package tr.com.huseyinaydin.application.pipeline.behavior;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import tr.com.huseyinaydin.application.cqrs.IPipelineBehavior;
import tr.com.huseyinaydin.application.cqrs.PipelineDelegate;
import tr.com.huseyinaydin.application.ports.IOutboxRepository;
import tr.com.huseyinaydin.domain.outbox.DomainEventOutbox;
import tr.com.huseyinaydin.sharedkernel.events.DomainEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

@Order(11)
public class DomainEventBehavior<TRequest, TResponse> implements IPipelineBehavior<TRequest, TResponse> {

    private static final Logger log = LoggerFactory.getLogger(DomainEventBehavior.class);
    private final IOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public DomainEventBehavior(IOutboxRepository outboxRepository, ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
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
                        saveToOutbox(event);
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
                                saveToOutbox(event);
                            }
                        }
                    } catch (NoSuchMethodException e) {
                        // ignore
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error saving domain events to outbox from result", e);
        }
    }

    private void saveToOutbox(DomainEvent event) {
        try {
            log.debug("Saving event to outbox: {}", event.getClass().getSimpleName());
            String payload = objectMapper.writeValueAsString(event);
            DomainEventOutbox outbox = new DomainEventOutbox(
                    event.getEventId(),
                    event.getAggregateType(),
                    event.getAggregateId(),
                    event.getClass().getName(), // Store full class name so we can deserialize it later
                    payload,
                    event.getOccurredAt()
            );
            outboxRepository.save(outbox);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize domain event: {}", event.getClass().getSimpleName(), e);
        }
    }
}
