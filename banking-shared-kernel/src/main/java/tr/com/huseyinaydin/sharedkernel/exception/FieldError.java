package tr.com.huseyinaydin.sharedkernel.exception;

public record FieldError(String field, String message, Object rejectedValue) {}
