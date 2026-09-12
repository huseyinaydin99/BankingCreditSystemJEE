package tr.com.huseyinaydin.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "banking.security.token-options")
public class TokenOptions {

    private String securityKey;
    private String issuer;
    private String audience;
    private int accessTokenExpiration;
    private int refreshTokenExpiration;

    public String getSecurityKey() { return securityKey; }
    public void setSecurityKey(String securityKey) { this.securityKey = securityKey; }

    public String getIssuer() { return issuer; }
    public void setIssuer(String issuer) { this.issuer = issuer; }

    public String getAudience() { return audience; }
    public void setAudience(String audience) { this.audience = audience; }

    public int getAccessTokenExpiration() { return accessTokenExpiration; }
    public void setAccessTokenExpiration(int accessTokenExpiration) { this.accessTokenExpiration = accessTokenExpiration; }

    public int getRefreshTokenExpiration() { return refreshTokenExpiration; }
    public void setRefreshTokenExpiration(int refreshTokenExpiration) { this.refreshTokenExpiration = refreshTokenExpiration; }
}
