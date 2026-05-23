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
import tr.com.huseyinaydin.sharedkernel.exception.ErrorResponse;
import tr.com.huseyinaydin.sharedkernel.exception.FieldError;
import tr.com.huseyinaydin.sharedkernel.exception.NotFoundException;
import tr.com.huseyinaydin.sharedkernel.exception.ValidationException;
import tr.com.huseyinaydin.sharedkernel.exception.ValidationErrorResponse;

import java.time.LocalDateTime;
import java.util.List;

// GlobalExceptionHandler sınıfı Neden Shared-Kernel Yerine Web Projesinde (Gösterim Katmanında) Konumlandırıldı?
/* CEVAP:
Bu mimaride asıl amaç yalnızca hataları yakalamak veya sınıfları katmanlara ayırmak değil,
sistemin düşünce yapısını teknoloji bağımlılıklarından arındırarak iş kurallarını merkeze yerleştirmektir.
Bu nedenle hata tipleri, domain olayları ve hata sözleşmeleri çekirdek yapıda konumlanırken,
HTTP’ye özgü davranışlar dış katmana bırakılır.
Özellikle GlobalExceptionHandler’ın shared-kernel yerine web yani gösterim katmanında bulunmasının nedeni,
onun iş kurallarını değil HTTP dünyasını anlamasıdır; çünkü istek–yanıt akışı, durum kodları,
hata gövdeleri ve istemciye sunulacak format tamamen sunum katmanının sorumluluğudur.
Shared-kernel yalnızca “hangi hata oluştuğunu” bilir,
fakat bu hatanın kullanıcıya 400, 401 veya 500 olarak nasıl yansıtılacağını bilmemelidir.
Aksi durumda çekirdek yapı teknoloji detaylarını içine çekmeye başlar ve zamanla bağımlılık zinciri oluşur.
Bu ayrım ilk bakışta daha katı görünse de uzun vadede sistemi
framework merkezli bir yapıya dönüştürmek yerine iş kuralları merkezli, bağımsız,
sürdürülebilir ve ölçeklenebilir bir mimariye dönüştüren kritik bir tasarım sınırı oluşturur.
*/

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
