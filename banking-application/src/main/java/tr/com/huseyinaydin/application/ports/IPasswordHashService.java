package tr.com.huseyinaydin.application.ports;

public interface IPasswordHashService {

    byte[] generateSalt();

    byte[] hashPassword(String plainPassword, byte[] salt);

    boolean verify(String plainPassword, byte[] hash, byte[] salt);
}
