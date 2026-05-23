package tr.com.huseyinaydin.sharedkernel.exception;

import java.time.LocalDateTime;

public record ErrorResponse(
        String errorCode,
        String message,
        LocalDateTime timestamp,
        String path
) {}
