package tr.com.huseyinaydin.sharedkernel.exception;

import java.util.Collections;
import java.util.List;

public class ValidationException extends ApplicationException {

    private static final String ERROR_CODE = "VALIDATION_ERROR";

    private final List<ValidationError> errors;

    public ValidationException(List<ValidationError> errors) {
        super("Doğrulama başarısız", ERROR_CODE);
        this.errors = Collections.unmodifiableList(errors);
    }

    public ValidationException(ValidationError error) {
        this(Collections.singletonList(error));
    }

    public ValidationException(String field, String message) {
        this(new ValidationError(field, message));
    }

    public ValidationException(String field, String message, Object rejectedValue) {
        this(new ValidationError(field, message, rejectedValue));
    }

    public List<ValidationError> getErrors() { return errors; }
}
