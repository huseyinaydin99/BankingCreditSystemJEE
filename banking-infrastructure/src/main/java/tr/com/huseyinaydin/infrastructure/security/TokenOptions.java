package tr.com.huseyinaydin.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TokenOptions {

    @Value("${security.token.secret-key}")
    private String secretKey;

    @Value("${security.token.issuer}")
    private String issuer;

    @Value("${security.token.audience}")
    private String audience;

    @Value("${security.token.access-token-expiration:30}")
    private int accessTokenExpiration;

    @Value("${security.token.refresh-token-expiration:7}")
    private int refreshTokenExpiration;

    public String getSecretKey() { return secretKey; }
    public String getIssuer() { return issuer; }
    public String getAudience() { return audience; }
    public int getAccessTokenExpiration() { return accessTokenExpiration; }
    public int getRefreshTokenExpiration() { return refreshTokenExpiration; }
}
