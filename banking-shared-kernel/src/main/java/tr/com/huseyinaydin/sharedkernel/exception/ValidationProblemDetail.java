package tr.com.huseyinaydin.sharedkernel.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * RFC 7807 {@link ProblemDetail} tipinin doğrulama hataları için genişletilmiş biçimi.
 *
 * Standart alanlara ek olarak {@code errors} uzantısını taşır: her anahtar hatalı bir
 * alanın adı, değer ise o alana ait insan-okur hata mesajlarının listesidir. Aynı alanda
 * birden çok kural ihlali olabileceği için değer {@code List<String>} olarak modellenir.
 *
 * RFC 7807 bölüm 3.2 "extension members" ilkesine göre {@code errors} bir uzantı üyesidir;
 * standart alanlarla aynı JSON nesnesi içinde düz olarak serileştirilir.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValidationProblemDetail extends ProblemDetail {

    private Map<String, List<String>> errors;

    protected ValidationProblemDetail() {
        // Jackson için
    }

    public ValidationProblemDetail(URI type, String title, int status, String detail,
                                   URI instance, Map<String, List<String>> errors) {
        super(type, title, status, detail, instance);
        this.errors = errors;
    }

    public Map<String, List<String>> getErrors() { return errors; }
}
