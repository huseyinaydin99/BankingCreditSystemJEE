package tr.com.huseyinaydin.web.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;

public class TestJwtTokenFactory {

    public static final String SECRET = "my-super-secret-key-that-is-very-long-32-bytes!!";
    public static final String ISSUER = "TestIssuer";
    public static final String AUDIENCE = "TestAudience";

    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes(UTF_8));

    public static String generateToken(UUID userId, List<String> roles) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", "test@test.com")
                .claim("roles", roles)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                .issuer(ISSUER)
                .audience().add(AUDIENCE).and()
                .signWith(KEY)
                .compact();
    }

    public static String generateAdminToken() {
        return generateToken(UUID.randomUUID(), List.of("ROLE_ADMIN"));
    }

    public static String generateOfficerToken() {
        return generateToken(UUID.randomUUID(), List.of("ROLE_OFFICER"));
    }

    public static String generateCustomerToken(UUID customerId) {
        return generateToken(customerId, List.of("ROLE_CUSTOMER"));
    }
}
