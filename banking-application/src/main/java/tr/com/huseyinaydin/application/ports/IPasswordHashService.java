package tr.com.huseyinaydin.application.ports;

public interface IPasswordHashService {

    PasswordHash createHash(String plainPassword);

    boolean verifyHash(String plainPassword, PasswordHash hash);
}
