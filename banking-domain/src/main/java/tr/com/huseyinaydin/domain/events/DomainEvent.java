package tr.com.huseyinaydin.domain.events;

import java.time.LocalDateTime;
import java.util.UUID;

public abstract class DomainEvent {

    private final UUID eventId;
    private final LocalDateTime occurredAt;
    private final String eventType;

    protected DomainEvent() {
        this.eventId = UUID.randomUUID();
        this.occurredAt = LocalDateTime.now();
        this.eventType = this.getClass().getSimpleName();
    }

    public UUID getEventId() { return eventId; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
    public String getEventType() { return eventType; }
}
