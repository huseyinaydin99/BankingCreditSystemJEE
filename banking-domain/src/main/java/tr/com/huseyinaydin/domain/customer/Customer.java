package tr.com.huseyinaydin.domain.customer;

import tr.com.huseyinaydin.domain.common.Entity;
import tr.com.huseyinaydin.sharedkernel.events.DomainEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public abstract class Customer extends Entity<UUID> {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$");

    private String phoneNumber;
    private String email;
    private String address;
    private boolean isActive;

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    protected Customer() {
        super();
        this.isActive = true;
    }

    public void updateContactInfo(String phoneNumber, String email, String address) {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Geçersiz e-posta formatı: " + email);
        }
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.address = address;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public String getPhoneNumber() { return phoneNumber; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }
    public boolean isActive() { return isActive; }

    public abstract String getFullName();

    protected void addDomainEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }

    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = new ArrayList<>(this.domainEvents);
        this.domainEvents.clear();
        return Collections.unmodifiableList(events);
    }
}
