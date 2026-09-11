package tr.com.huseyinaydin.domain.token;





import java.time.LocalDateTime;
import java.util.UUID;



public class RefreshToken {

    private UUID id;

    private UUID userId;

    private String token;

    private LocalDateTime expiration;

    private boolean isRevoked;

    protected RefreshToken() {}

    public RefreshToken(UUID userId, String token, LocalDateTime expiration) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.token = token;
        this.expiration = expiration;
        this.isRevoked = false;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getToken() { return token; }
    public LocalDateTime getExpiration() { return expiration; }
    public boolean isRevoked() { return isRevoked; }

    public void revoke() { this.isRevoked = true; }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiration);
    }
}
