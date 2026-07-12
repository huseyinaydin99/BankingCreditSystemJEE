package tr.com.huseyinaydin.sharedkernel.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.net.URI;

/**
 * RFC 7807 {@link ProblemDetail} tipinin, uygulamaya özgü hata kodu taşıyan biçimi.
 *
 * Standart alanlara ek olarak {@code errorCode} uzantısını taşır. Bu kod
 * ({@code ApplicationException.getErrorCode()}) istemcilerin hatayı programatik olarak
 * ayırt edebilmesi için sabittir ve dile bağlı olmayan bir tanımlayıcıdır; {@code detail}
 * ise değişebilen, insan-okur açıklamadır.
 *
 * {@code type} alanı problem sınıfını (URI) tarif ederken {@code errorCode} aynı sınıf
 * içindeki alt durumları ayırt etmek için kullanılabilir.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BusinessProblemDetail extends ProblemDetail {

    private String errorCode;

    protected BusinessProblemDetail() {
        // Jackson için
    }

    public BusinessProblemDetail(URI type, String title, int status, String detail,
                                 URI instance, String errorCode) {
        super(type, title, status, detail, instance);
        this.errorCode = errorCode;
    }

    public String getErrorCode() { return errorCode; }
}
