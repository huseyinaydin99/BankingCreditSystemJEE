package tr.com.huseyinaydin.application.ports;

import java.time.LocalDateTime;

public record AccessToken(String token, LocalDateTime expiration, String refreshToken) {}
