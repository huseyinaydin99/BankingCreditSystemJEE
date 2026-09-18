package tr.com.huseyinaydin.infrastructure.jobs;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.ports.IEventPublisher;
import tr.com.huseyinaydin.application.ports.IOutboxRepository;
import tr.com.huseyinaydin.domain.outbox.DomainEventOutbox;
import tr.com.huseyinaydin.sharedkernel.events.DomainEvent;

import java.util.List;

@Component
public class OutboxProcessorJob {

    private static final Logger log = LoggerFactory.getLogger(OutboxProcessorJob.class);

    private final IOutboxRepository outboxRepository;
    private final IEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    public OutboxProcessorJob(IOutboxRepository outboxRepository,
                              IEventPublisher eventPublisher,
                              ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelayString = "${banking.jobs.outbox-processor.delay:5000}")
    @org.springframework.transaction.annotation.Transactional
    public void processOutboxMessages() {
        List<DomainEventOutbox> messages = outboxRepository.findUnprocessed();
        if (messages.isEmpty()) {
            return;
        }

        log.info("Found {} unprocessed outbox messages.", messages.size());

        for (DomainEventOutbox message : messages) {
            try {
                Class<?> eventClass = Class.forName(message.getEventType());
                Object eventObject = objectMapper.readValue(message.getPayload(), eventClass);
                
                if (eventObject instanceof DomainEvent) {
                    eventPublisher.publish((DomainEvent) eventObject);
                    outboxRepository.markProcessed(message.getId());
                    log.debug("Successfully processed outbox message {}", message.getId());
                } else {
                    outboxRepository.incrementRetry(message.getId(), "Event class is not a DomainEvent");
                }
            } catch (Exception e) {
                log.error("Failed to process outbox message {}", message.getId(), e);
                outboxRepository.incrementRetry(message.getId(), e.getMessage());
            }
        }
    }
}
