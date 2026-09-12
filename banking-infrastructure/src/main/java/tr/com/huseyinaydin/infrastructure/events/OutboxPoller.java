package tr.com.huseyinaydin.infrastructure.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tr.com.huseyinaydin.application.ports.IEventPublisher;
import tr.com.huseyinaydin.application.ports.IOutboxRepository;
import tr.com.huseyinaydin.domain.outbox.DomainEventOutbox;
import tr.com.huseyinaydin.sharedkernel.events.DomainEvent;

import java.util.List;

@Component
public class OutboxPoller {

    private static final Logger log = LoggerFactory.getLogger(OutboxPoller.class);
    private final IOutboxRepository outboxRepository;
    private final IEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    public OutboxPoller(IOutboxRepository outboxRepository, IEventPublisher eventPublisher, ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processOutbox() {
        List<DomainEventOutbox> unprocessed = outboxRepository.findUnprocessed();
        if (unprocessed.isEmpty()) {
            return;
        }

        log.debug("Found {} unprocessed domain events in outbox", unprocessed.size());

        for (DomainEventOutbox outbox : unprocessed) {
            try {
                Class<?> eventClass = Class.forName(outbox.getEventType());
                DomainEvent event = (DomainEvent) objectMapper.readValue(outbox.getPayload(), eventClass);
                
                eventPublisher.publish(event);
                
                outboxRepository.markProcessed(outbox.getId());
                log.debug("Successfully processed outbox event: {}", outbox.getId());
            } catch (Exception e) {
                log.error("Failed to process outbox event: {}", outbox.getId(), e);
                outboxRepository.incrementRetry(outbox.getId(), e.getMessage());
            }
        }
    }
}
