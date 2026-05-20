package tr.com.huseyinaydin.application.ports;

public interface IOtpService {

    String generateSecret();

    String generateTotp(String base32Secret);

    boolean verifyTotp(String base32Secret, String totp);
}
