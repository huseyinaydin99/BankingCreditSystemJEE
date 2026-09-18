package tr.com.huseyinaydin.domain.events;

import tr.com.huseyinaydin.sharedkernel.events.DomainEvent;
import java.util.UUID;

public class CorporateCustomerCreatedEvent extends DomainEvent {
    private final UUID customerId;
    private final String companyName;
    private final String taxNumber;
    private final String email;
    private final byte[] passwordHash;
    private final byte[] passwordSalt;

    protected CorporateCustomerCreatedEvent() { this.customerId = null; this.companyName = null; this.taxNumber = null; this.email = null; this.passwordHash = null; this.passwordSalt = null; }

    public CorporateCustomerCreatedEvent(UUID customerId, String companyName, String taxNumber, String email, byte[] passwordHash, byte[] passwordSalt) {
        super();
        this.customerId = customerId;
        this.companyName = companyName;
        this.taxNumber = taxNumber;
        this.email = email;
        this.passwordHash = passwordHash;
        this.passwordSalt = passwordSalt;
    }

    @Override
    public String getAggregateId() { return customerId.toString(); }

    @Override
    public String getAggregateType() { return "CorporateCustomer"; }

    public UUID getCustomerId() { return customerId; }
    public String getCompanyName() { return companyName; }
    public String getTaxNumber() { return taxNumber; }
    public String getEmail() { return email; }
    public byte[] getPasswordHash() { return passwordHash; }
    public byte[] getPasswordSalt() { return passwordSalt; }
}
