package tr.com.huseyinaydin.web.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import tr.com.huseyinaydin.sharedkernel.exception.AuthorizationException;
import tr.com.huseyinaydin.sharedkernel.exception.BusinessException;
import tr.com.huseyinaydin.sharedkernel.exception.ConflictException;
import tr.com.huseyinaydin.sharedkernel.exception.NotFoundException;
import tr.com.huseyinaydin.sharedkernel.exception.ValidationException;

import java.time.LocalDateTime;
import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex,
                                                         HttpServletRequest request) {
        return ResponseEntity.badRequest().body(
                new ErrorResponse(ex.getErrorCode(), ex.getMessage(),
                        LocalDateTime.now(), request.getRequestURI()));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(ValidationException ex) {
        List<FieldError> errors = ex.getErrors().stream()
                .map(e -> new FieldError(e.field(), e.message(), e.rejectedValue()))
                .toList();
        return ResponseEntity.badRequest()
                .body(new ValidationErrorResponse(errors, LocalDateTime.now()));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex,
                                                         HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ErrorResponse(ex.getErrorCode(), ex.getMessage(),
                        LocalDateTime.now(), request.getRequestURI()));
    }

    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<ErrorResponse> handleAuthorization(AuthorizationException ex,
                                                              HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new ErrorResponse(ex.getErrorCode(), ex.getMessage(),
                        LocalDateTime.now(), request.getRequestURI()));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex,
                                                         HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new ErrorResponse(ex.getErrorCode(), ex.getMessage(),
                        LocalDateTime.now(), request.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex,
                                                        HttpServletRequest request) {
        log.error("Beklenmeyen hata [{}]: {}", request.getRequestURI(), ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ErrorResponse("INTERNAL_ERROR", "Sunucu hatası oluştu",
                        LocalDateTime.now(), request.getRequestURI()));
    }
}
