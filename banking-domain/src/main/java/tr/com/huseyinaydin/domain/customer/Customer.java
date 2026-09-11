package tr.com.huseyinaydin.domain.customer;

import tr.com.huseyinaydin.domain.common.Entity;







import java.util.UUID;
import java.util.regex.Pattern;





public abstract class Customer extends Entity<UUID> {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$");

    private String phoneNumber;

    private String email;

    private String address;

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
