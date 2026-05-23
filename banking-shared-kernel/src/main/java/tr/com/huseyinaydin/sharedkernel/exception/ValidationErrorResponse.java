package tr.com.huseyinaydin.sharedkernel.exception;

import java.time.LocalDateTime;
import java.util.List;

public record ValidationErrorResponse(
        List<FieldError> errors,
        LocalDateTime timestamp
) {}
