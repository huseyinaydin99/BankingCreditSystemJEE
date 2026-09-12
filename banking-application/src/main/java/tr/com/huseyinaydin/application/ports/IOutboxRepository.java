package tr.com.huseyinaydin.application.ports;

import tr.com.huseyinaydin.domain.outbox.DomainEventOutbox;

import java.util.List;
import java.util.UUID;

public interface IOutboxRepository {
    void save(DomainEventOutbox outbox);
    List<DomainEventOutbox> findUnprocessed();
    void markProcessed(UUID id);
    void incrementRetry(UUID id, String error);
}
