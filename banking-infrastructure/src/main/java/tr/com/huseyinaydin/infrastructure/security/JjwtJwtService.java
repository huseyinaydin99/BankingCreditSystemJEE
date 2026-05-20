package tr.com.huseyinaydin.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import tr.com.huseyinaydin.application.ports.AccessToken;
import tr.com.huseyinaydin.application.ports.IJwtService;
import tr.com.huseyinaydin.domain.user.ApplicationUser;
import tr.com.huseyinaydin.sharedkernel.exception.BusinessException;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service
public class JjwtJwtService implements IJwtService {

    private static final String ROLES_CLAIM = "roles";
    private static final String EMAIL_CLAIM = "email";
    private static final String JWT_ERROR = "JWT_ERROR";

    private final TokenOptions tokenOptions;
    private final SecretKey signingKey;

    public JjwtJwtService(TokenOptions tokenOptions) {
        this.tokenOptions = tokenOptions;
        this.signingKey = Keys.hmacShaKeyFor(tokenOptions.getSecretKey().getBytes(UTF_8));
    }

    @Override
    public AccessToken createToken(ApplicationUser user, List<String> roles) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiration = now.plusMinutes(tokenOptions.getAccessTokenExpiration());

        String token = Jwts.builder()
                .subject(user.getId().toString())
                .claim(EMAIL_CLAIM, user.getEmail())
                .claim(ROLES_CLAIM, roles)
                .issuedAt(toDate(now))
                .expiration(toDate(expiration))
                .issuer(tokenOptions.getIssuer())
                .audience().add(tokenOptions.getAudience()).and()
                .signWith(signingKey)
                .compact();

        String refreshToken = UUID.randomUUID().toString();
        return new AccessToken(token, expiration, refreshToken);
    }

    @Override
    public Claims validateToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new BusinessException("JWT token süresi doldu", JWT_ERROR, e);
        } catch (MalformedJwtException e) {
            throw new BusinessException("Geçersiz JWT token formatı", JWT_ERROR, e);
        } catch (JwtException e) {
            throw new BusinessException("JWT doğrulama hatası: " + e.getMessage(), JWT_ERROR, e);
        }
    }

    @Override
    public UUID extractUserId(String token) {
        return UUID.fromString(validateToken(token).getSubject());
    }

    private Date toDate(LocalDateTime ldt) {
        return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
    }
}
