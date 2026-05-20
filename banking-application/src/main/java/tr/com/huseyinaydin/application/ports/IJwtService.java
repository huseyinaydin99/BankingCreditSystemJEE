package tr.com.huseyinaydin.application.ports;

import io.jsonwebtoken.Claims;
import tr.com.huseyinaydin.domain.user.ApplicationUser;

import java.util.List;
import java.util.UUID;

public interface IJwtService {

    AccessToken createToken(ApplicationUser user, List<String> roles);

    Claims validateToken(String token);

    UUID extractUserId(String token);
}
