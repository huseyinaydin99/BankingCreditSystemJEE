package tr.com.huseyinaydin.domain.events;

import tr.com.huseyinaydin.sharedkernel.events.DomainEvent;
import java.util.UUID;

public class CreditApplicationRejectedEvent extends DomainEvent {
    private final UUID applicationId;
    private final String reason;

    public CreditApplicationRejectedEvent(UUID applicationId, String reason) {
        super();
        this.applicationId = applicationId;
        this.reason = reason;
    }

    @Override
    public String getAggregateId() { return applicationId.toString(); }

    @Override
    public String getAggregateType() { return "CreditApplication"; }

    public UUID getApplicationId() { return applicationId; }
    public String getReason() { return reason; }
}
