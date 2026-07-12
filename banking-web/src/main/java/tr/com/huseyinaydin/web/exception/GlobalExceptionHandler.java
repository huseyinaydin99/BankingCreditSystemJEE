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

/*
 * Tüm hata yanıtları RFC 7807 "Problem Details for HTTP APIs" formatında,
 * "application/problem+json" ortam tipiyle döndürülür. Problem tipleri, başlıkları ve
 * durum kodları {@link BankingErrorTypes} kataloğundan gelir; böylece sözleşme tek
 * kaynaktan yönetilir. İstisna hiyerarşisi (BusinessException, ValidationException,
 * NotFoundException vb.) olduğu gibi korunur — burada yalnızca HTTP yanıt formatı
 * standardize edilir.
 */
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
        // Dahili hatanın ayrıntısı istemciye sızdırılmaz; genel bir mesaj döndürülür.
        return business(BankingErrorTypes.INTERNAL_ERROR, "Sunucu hatası oluştu",
                "INTERNAL_ERROR", request);
    }

    /* ----------------------------------------------------------------------
       Yardımcılar
       ---------------------------------------------------------------------- */

    /** Uygulama hata kodu taşıyan (validation dışı) tüm problemler için ortak kurucu. */
    private static ResponseEntity<ProblemDetail> business(BankingErrorTypes type,
                                                          String detail,
                                                          String errorCode,
                                                          HttpServletRequest request) {
        BusinessProblemDetail body = new BusinessProblemDetail(
                type.type(), type.title(), type.status(), detail,
                instance(request), errorCode);
        return problem(type.status(), body);
    }

    /** RFC 7807 ortam tipiyle (application/problem+json) yanıtı sarmalar. */
    private static ResponseEntity<ProblemDetail> problem(int status, ProblemDetail body) {
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(body);
    }

    /** Problemin oluştuğu somut isteğin URI'si (RFC 7807 {@code instance}). */
    private static URI instance(HttpServletRequest request) {
        return URI.create(request.getRequestURI());
    }
}
