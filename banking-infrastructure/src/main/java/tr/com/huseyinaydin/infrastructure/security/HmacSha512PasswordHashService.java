package tr.com.huseyinaydin.infrastructure.security;

import org.springframework.stereotype.Service;
import tr.com.huseyinaydin.application.ports.IPasswordHashService;
import tr.com.huseyinaydin.application.ports.PasswordHash;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service
public class HmacSha512PasswordHashService implements IPasswordHashService {

    private static final String ALGORITHM = "HmacSHA512";
    private static final int SALT_LENGTH = 64;

    @Override
    public PasswordHash createHash(String plainPassword) {
        byte[] salt = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(salt);
        byte[] hash = doHmac(plainPassword.getBytes(UTF_8), salt);
        return new PasswordHash(hash, salt);
    }

    @Override
    public boolean verifyHash(String plainPassword, PasswordHash passwordHash) {
        byte[] computed = doHmac(plainPassword.getBytes(UTF_8), passwordHash.salt());
        return MessageDigest.isEqual(computed, passwordHash.hash());
    }

    private byte[] doHmac(byte[] data, byte[] salt) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(salt, ALGORITHM));
            return mac.doFinal(data);
        } catch (Exception e) {
            throw new IllegalStateException("HMAC-SHA512 hash hesaplanamadı", e);
        }
    }
}
