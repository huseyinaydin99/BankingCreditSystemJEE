package tr.com.huseyinaydin.domain.events;

import tr.com.huseyinaydin.sharedkernel.events.DomainEvent;
import java.math.BigDecimal;
import java.util.UUID;

public class CreditApplicationCreatedEvent extends DomainEvent {
    private final UUID applicationId;
    private final UUID customerId;
    private final UUID creditTypeId;
    private final BigDecimal requestedAmount;
    private final int requestedTerm;

    public CreditApplicationCreatedEvent(UUID applicationId, UUID customerId, UUID creditTypeId, BigDecimal requestedAmount, int requestedTerm) {
        super();
        this.applicationId = applicationId;
        this.customerId = customerId;
        this.creditTypeId = creditTypeId;
        this.requestedAmount = requestedAmount;
        this.requestedTerm = requestedTerm;
    }

    @Override
    public String getAggregateId() { return applicationId.toString(); }

    @Override
    public String getAggregateType() { return "CreditApplication"; }

    public UUID getApplicationId() { return applicationId; }
    public UUID getCustomerId() { return customerId; }
    public UUID getCreditTypeId() { return creditTypeId; }
    public BigDecimal getRequestedAmount() { return requestedAmount; }
    public int getRequestedTerm() { return requestedTerm; }
}
