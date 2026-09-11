package tr.com.huseyinaydin.domain.user;

import tr.com.huseyinaydin.domain.common.Entity;
import tr.com.huseyinaydin.domain.enums.UserRole;





import java.util.UUID;



public class ApplicationUser extends Entity<UUID> {

    private UUID customerId;

    private String email;

    private byte[] passwordHash;

    private byte[] passwordSalt;

    private boolean isActive;

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
