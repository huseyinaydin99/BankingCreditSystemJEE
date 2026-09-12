package tr.com.huseyinaydin.domain.events;

import tr.com.huseyinaydin.sharedkernel.events.DomainEvent;
import tr.com.huseyinaydin.domain.valueobjects.Money;
import java.math.BigDecimal;
import java.util.UUID;

public class CreditApplicationApprovedEvent extends DomainEvent {
    private final UUID applicationId;
    private final Money approvedAmount;
    private final Integer approvedTerm;
    private final BigDecimal interestRate;

    public CreditApplicationApprovedEvent(UUID applicationId, Money approvedAmount, Integer approvedTerm, BigDecimal interestRate) {
        super();
        this.applicationId = applicationId;
        this.approvedAmount = approvedAmount;
        this.approvedTerm = approvedTerm;
        this.interestRate = interestRate;
    }

    @Override
    public String getAggregateId() { return applicationId.toString(); }

    @Override
    public String getAggregateType() { return "CreditApplication"; }

    public UUID getApplicationId() { return applicationId; }
    public Money getApprovedAmount() { return approvedAmount; }
    public Integer getApprovedTerm() { return approvedTerm; }
    public BigDecimal getInterestRate() { return interestRate; }
}
