package tr.com.huseyinaydin.domain.events;

import tr.com.huseyinaydin.sharedkernel.events.DomainEvent;
import java.util.UUID;

public class IndividualCustomerCreatedEvent extends DomainEvent {
    private final UUID customerId;
    private final String firstName;
    private final String lastName;
    private final String nationalId;
    private final String email;

    public IndividualCustomerCreatedEvent(UUID customerId, String firstName, String lastName, String nationalId, String email) {
        super();
        this.customerId = customerId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.nationalId = nationalId;
        this.email = email;
    }

    @Override
    public String getAggregateId() { return customerId.toString(); }

    @Override
    public String getAggregateType() { return "IndividualCustomer"; }

    public UUID getCustomerId() { return customerId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getNationalId() { return nationalId; }
    public String getEmail() { return email; }
}
