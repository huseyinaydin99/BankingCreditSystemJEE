package tr.com.huseyinaydin.application.ports;

public record PasswordHash(byte[] hash, byte[] salt) {}
