package tr.com.huseyinaydin.infrastructure.security;

import org.springframework.stereotype.Service;
import tr.com.huseyinaydin.application.ports.IOtpService;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.time.Instant;

/**
 * RFC 6238 TOTP (Time-based One-Time Password) implementasyonu.
 * HMAC-SHA1 kullanır; dış kütüphane gerektirmez.
 * Secret, RFC 4648 Base32 formatında saklanır (TOTP authenticator uyumluluğu için).
 */
@Service
public class TotpServiceImpl implements IOtpService {

    private static final int OTP_DIGITS = 6;
    private static final int TIME_STEP_SECONDS = 30;
    private static final int CLOCK_SKEW_STEPS = 1;
    private static final String HMAC_ALGORITHM = "HmacSHA1";
    private static final int SECRET_BYTE_LENGTH = 20;

    private static final String BASE32_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

    @Override
    public String generateSecret() {
        byte[] bytes = new byte[SECRET_BYTE_LENGTH];
        new SecureRandom().nextBytes(bytes);
        return base32Encode(bytes);
    }

    @Override
    public String generateTotp(String base32Secret) {
        long timeStep = Instant.now().getEpochSecond() / TIME_STEP_SECONDS;
        return computeTotp(base32Decode(base32Secret), timeStep);
    }

    @Override
    public boolean verifyTotp(String base32Secret, String totp) {
        byte[] key = base32Decode(base32Secret);
        long timeStep = Instant.now().getEpochSecond() / TIME_STEP_SECONDS;
        for (int delta = -CLOCK_SKEW_STEPS; delta <= CLOCK_SKEW_STEPS; delta++) {
            if (computeTotp(key, timeStep + delta).equals(totp)) return true;
        }
        return false;
    }

    private String computeTotp(byte[] key, long timeStep) {
        byte[] hmac = hmacSha1(key, longToBytes(timeStep));
        int offset = hmac[hmac.length - 1] & 0x0F;
        int binary = ((hmac[offset]     & 0x7F) << 24)
                   | ((hmac[offset + 1] & 0xFF) << 16)
                   | ((hmac[offset + 2] & 0xFF) << 8)
                   |  (hmac[offset + 3] & 0xFF);
        int otp = binary % (int) Math.pow(10, OTP_DIGITS);
        return String.format("%0" + OTP_DIGITS + "d", otp);
    }

    private byte[] hmacSha1(byte[] key, byte[] data) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(key, HMAC_ALGORITHM));
            return mac.doFinal(data);
        } catch (Exception e) {
            throw new IllegalStateException("HMAC-SHA1 hesaplanamadı", e);
        }
    }

    private byte[] longToBytes(long value) {
        byte[] result = new byte[8];
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte) (value & 0xFF);
            value >>= 8;
        }
        return result;
    }

    private String base32Encode(byte[] data) {
        StringBuilder sb = new StringBuilder();
        int buffer = 0, bitsLeft = 0;
        for (byte b : data) {
            buffer = (buffer << 8) | (b & 0xFF);
            bitsLeft += 8;
            while (bitsLeft >= 5) {
                bitsLeft -= 5;
                sb.append(BASE32_ALPHABET.charAt((buffer >> bitsLeft) & 0x1F));
            }
        }
        if (bitsLeft > 0) {
            sb.append(BASE32_ALPHABET.charAt((buffer << (5 - bitsLeft)) & 0x1F));
        }
        return sb.toString();
    }

    private byte[] base32Decode(String encoded) {
        encoded = encoded.toUpperCase().replaceAll("[=\\s]", "");
        byte[] result = new byte[encoded.length() * 5 / 8];
        int buffer = 0, bitsLeft = 0, index = 0;
        for (char c : encoded.toCharArray()) {
            int val = BASE32_ALPHABET.indexOf(c);
            if (val < 0) continue;
            buffer = (buffer << 5) | val;
            bitsLeft += 5;
            if (bitsLeft >= 8) {
                bitsLeft -= 8;
                result[index++] = (byte) ((buffer >> bitsLeft) & 0xFF);
            }
        }
        return result;
    }
}
