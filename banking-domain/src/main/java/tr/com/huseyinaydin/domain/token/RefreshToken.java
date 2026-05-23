package tr.com.huseyinaydin.domain.token;

// import jakarta.persistence.Column;  — META-INF/orm/RefreshToken.xml ile eşleme sağlanmaktadır.
// import jakarta.persistence.Id;
// import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

// @jakarta.persistence.Entity
// @Table(name = "REFRESH_TOKENS")
public class RefreshToken {

    // @Id
    // @Column(name = "ID")
    private UUID id;

    // @Column(name = "USER_ID", nullable = false)
    private UUID userId;

    // @Column(name = "TOKEN", nullable = false, unique = true, length = 512)
    private String token;

    // @Column(name = "EXPIRATION", nullable = false)
    private LocalDateTime expiration;

    // @Column(name = "IS_REVOKED", nullable = false)
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
