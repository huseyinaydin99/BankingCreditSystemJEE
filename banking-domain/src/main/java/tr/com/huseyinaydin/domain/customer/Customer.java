package tr.com.huseyinaydin.domain.customer;

import tr.com.huseyinaydin.domain.common.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

import java.util.UUID;
import java.util.regex.Pattern;

@jakarta.persistence.Entity
@Table(name = "CUSTOMERS")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "CUSTOMER_TYPE_CODE", discriminatorType = DiscriminatorType.STRING)
public abstract class Customer extends Entity<UUID> {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$");

    @Column(name = "PHONE_NUMBER", length = 20)
    private String phoneNumber;

    @Column(name = "EMAIL", nullable = false, length = 150)
    private String email;

    @Column(name = "ADDRESS", length = 500)
    private String address;

    @Column(name = "IS_ACTIVE", nullable = false)
    private boolean isActive;

    protected Customer() {
        super();
        this.isActive = true;
    }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmail() { return email; }

    public void setEmail(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Geçersiz e-posta formatı: " + email);
        }
        this.email = email;
    }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }

    public abstract String getFullName();
}
