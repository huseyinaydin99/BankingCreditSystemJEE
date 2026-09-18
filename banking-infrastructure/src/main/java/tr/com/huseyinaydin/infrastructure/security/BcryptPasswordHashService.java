package tr.com.huseyinaydin.infrastructure.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tr.com.huseyinaydin.application.ports.IPasswordHashService;
import tr.com.huseyinaydin.application.ports.PasswordHash;

import java.nio.charset.StandardCharsets;

@Service
public class BcryptPasswordHashService implements IPasswordHashService {

    private final PasswordEncoder passwordEncoder;

    public BcryptPasswordHashService() {
        this.passwordEncoder = new BCryptPasswordEncoder(12); // Work factor 12
    }

    @Override
    public PasswordHash createHash(String plainPassword) {
        String encoded = passwordEncoder.encode(plainPassword);
        // BCrypt stores the salt inside the encoded string, so we don't need a separate salt array.
        return new PasswordHash(encoded.getBytes(StandardCharsets.UTF_8), new byte[0]);
    }

    @Override
    public boolean verifyHash(String plainPassword, PasswordHash passwordHash) {
        String encoded = new String(passwordHash.hash(), StandardCharsets.UTF_8);
        return passwordEncoder.matches(plainPassword, encoded);
    }
}
