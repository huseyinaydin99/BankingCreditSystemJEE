package tr.com.huseyinaydin.sharedkernel.messaging;

import java.time.LocalDateTime;
import java.util.UUID;

public interface IDomainEvent {
    UUID getEventId();
    LocalDateTime getOccurredAt();
    String getEventType();
}
