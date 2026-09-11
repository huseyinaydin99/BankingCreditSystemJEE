package tr.com.huseyinaydin.web.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import tr.com.huseyinaydin.sharedkernel.exception.AuthorizationException;
import tr.com.huseyinaydin.sharedkernel.exception.BankingErrorTypes;
import tr.com.huseyinaydin.sharedkernel.exception.BusinessException;
import tr.com.huseyinaydin.sharedkernel.exception.BusinessProblemDetail;
import tr.com.huseyinaydin.sharedkernel.exception.ConflictException;
import tr.com.huseyinaydin.sharedkernel.exception.NotFoundException;
import tr.com.huseyinaydin.sharedkernel.exception.ProblemDetail;
import tr.com.huseyinaydin.sharedkernel.exception.ValidationException;
import tr.com.huseyinaydin.sharedkernel.exception.ValidationProblemDetail;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;





@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ProblemDetail> handleBusiness(BusinessException ex,
                                                        HttpServletRequest request) {
        return business(BankingErrorTypes.BUSINESS_RULE_VIOLATION, ex.getMessage(),
                ex.getErrorCode(), request);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ProblemDetail> handleValidation(ValidationException ex,
                                                          HttpServletRequest request) {
        BankingErrorTypes type = BankingErrorTypes.VALIDATION_FAILED;

        Map<String, List<String>> errors = ex.getErrors().stream()
                .collect(Collectors.groupingBy(
                        e -> e.field(),
                        Collectors.mapping(e -> e.message(), Collectors.toList())));

        ValidationProblemDetail body = new ValidationProblemDetail(
                type.type(), type.title(), type.status(), ex.getMessage(),
                instance(request), errors);

        return problem(type.status(), body);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(NotFoundException ex,
                                                        HttpServletRequest request) {
        return business(BankingErrorTypes.NOT_FOUND, ex.getMessage(),
                ex.getErrorCode(), request);
    }

    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<ProblemDetail> handleAuthorization(AuthorizationException ex,
                                                             HttpServletRequest request) {
        return business(BankingErrorTypes.UNAUTHORIZED, ex.getMessage(),
                ex.getErrorCode(), request);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ProblemDetail> handleConflict(ConflictException ex,
                                                        HttpServletRequest request) {
        return business(BankingErrorTypes.CONFLICT, ex.getMessage(),
                ex.getErrorCode(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGeneral(Exception ex,
                                                       HttpServletRequest request) {
        log.error("Beklenmeyen hata [{}]: {}", request.getRequestURI(), ex.getMessage(), ex);
        return business(BankingErrorTypes.INTERNAL_ERROR, "Sunucu hatası oluştu",
                "INTERNAL_ERROR", request);
    }


    private static ResponseEntity<ProblemDetail> business(BankingErrorTypes type,
                                                          String detail,
                                                          String errorCode,
                                                          HttpServletRequest request) {
        BusinessProblemDetail body = new BusinessProblemDetail(
                type.type(), type.title(), type.status(), detail,
                instance(request), errorCode);
        return problem(type.status(), body);
    }

    private static ResponseEntity<ProblemDetail> problem(int status, ProblemDetail body) {
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(body);
    }

    private static URI instance(HttpServletRequest request) {
        return URI.create(request.getRequestURI());
    }
}
