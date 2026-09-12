package tr.com.huseyinaydin.domain.events;

import tr.com.huseyinaydin.sharedkernel.events.DomainEvent;
import java.util.UUID;

public class CorporateCustomerCreatedEvent extends DomainEvent {
    private final UUID customerId;
    private final String companyName;
    private final String taxNumber;
    private final String email;

    public CorporateCustomerCreatedEvent(UUID customerId, String companyName, String taxNumber, String email) {
        super();
        this.customerId = customerId;
        this.companyName = companyName;
        this.taxNumber = taxNumber;
        this.email = email;
    }

    @Override
    public String getAggregateId() { return customerId.toString(); }

    @Override
    public String getAggregateType() { return "CorporateCustomer"; }

    public UUID getCustomerId() { return customerId; }
    public String getCompanyName() { return companyName; }
    public String getTaxNumber() { return taxNumber; }
    public String getEmail() { return email; }
}
