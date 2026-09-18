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

    public void changeRole(UserRole role) {
        this.role = role;
    }

    public void updatePassword(byte[] passwordHash, byte[] passwordSalt) {
        this.passwordHash = passwordHash;
        this.passwordSalt = passwordSalt;
    }

    public void updateEmail(String email) {
        this.email = email;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public UUID getCustomerId() { return customerId; }
    public String getEmail() { return email; }
    public byte[] getPasswordHash() { return passwordHash; }
    public byte[] getPasswordSalt() { return passwordSalt; }
    public boolean isActive() { return isActive; }
    public UserRole getRole() { return role; }
}
