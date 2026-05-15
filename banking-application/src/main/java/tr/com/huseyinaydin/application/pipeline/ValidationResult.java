package tr.com.huseyinaydin.application.pipeline;

import tr.com.huseyinaydin.sharedkernel.exception.ValidationError;

import java.util.Collections;
import java.util.List;

public final class ValidationResult {

    private final List<ValidationError> errors;

    private ValidationResult(List<ValidationError> errors) {
        this.errors = Collections.unmodifiableList(errors);
    }

    public static ValidationResult success() {
        return new ValidationResult(Collections.emptyList());
    }

    public static ValidationResult failure(List<ValidationError> errors) {
        return new ValidationResult(errors);
    }

    public static ValidationResult failure(String field, String message) {
        return new ValidationResult(Collections.singletonList(new ValidationError(field, message)));
    }

    public static ValidationResult failure(String field, String message, Object rejectedValue) {
        return new ValidationResult(
                Collections.singletonList(new ValidationError(field, message, rejectedValue)));
    }

    public boolean isValid() { return errors.isEmpty(); }
    public List<ValidationError> getErrors() { return errors; }
}
