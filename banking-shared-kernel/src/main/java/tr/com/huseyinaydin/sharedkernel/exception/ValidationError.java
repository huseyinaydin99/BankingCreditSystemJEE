package tr.com.huseyinaydin.sharedkernel.exception;

public record ValidationError(String field, String message, Object rejectedValue) {

    public ValidationError(String field, String message) {
        this(field, message, null);
    }
}
