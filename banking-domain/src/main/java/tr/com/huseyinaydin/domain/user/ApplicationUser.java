package tr.com.huseyinaydin.domain.user;

import tr.com.huseyinaydin.domain.common.Entity;
import tr.com.huseyinaydin.domain.enums.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.util.UUID;

@jakarta.persistence.Entity
@Table(name = "APPLICATION_USERS")
public class ApplicationUser extends Entity<UUID> {

    @Column(name = "CUSTOMER_ID")
    private UUID customerId;

    @Column(name = "EMAIL", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "PASSWORD_HASH", columnDefinition = "RAW(64)")
    private byte[] passwordHash;

    @Column(name = "PASSWORD_SALT", columnDefinition = "RAW(64)")
    private byte[] passwordSalt;

    @Column(name = "IS_ACTIVE", nullable = false)
    private boolean isActive;

    @Enumerated(EnumType.STRING)
    @Column(name = "ROLE", nullable = false, length = 20)
    private UserRole role;

    protected ApplicationUser() {
        super();
    }

    public ApplicationUser(UUID customerId, String email,
                           byte[] passwordHash, byte[] passwordSalt, UserRole role) {
        super();
        this.id = UUID.randomUUID();
        this.customerId = customerId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.passwordSalt = passwordSalt;
        this.role = role;
        this.isActive = true;
    }

    public UUID getCustomerId() { return customerId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public byte[] getPasswordHash() { return passwordHash; }
    public void setPasswordHash(byte[] passwordHash) { this.passwordHash = passwordHash; }

    public byte[] getPasswordSalt() { return passwordSalt; }
    public void setPasswordSalt(byte[] passwordSalt) { this.passwordSalt = passwordSalt; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
}
