package tr.com.huseyinaydin.web.exception;

public record FieldError(String field, String message, Object rejectedValue) {}
