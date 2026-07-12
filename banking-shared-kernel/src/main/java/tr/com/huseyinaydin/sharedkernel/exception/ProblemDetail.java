package tr.com.huseyinaydin.sharedkernel.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.net.URI;

/**
 * RFC 7807 "Problem Details for HTTP APIs" temel yanıt modeli.
 *
 * Bu tip, HTTP hata gövdelerini makine tarafından işlenebilir standart bir sözleşmeye
 * bağlar. shared-kernel yalnızca hatanın <em>ne olduğunu</em> tarif eder; hangi HTTP
 * durum koduyla ve hangi ortam tipiyle (application/problem+json) sunulacağı
 * gösterim katmanının (GlobalExceptionHandler) sorumluluğudur.
 *
 * Alanlar RFC 7807 bölüm 3.1 ile birebir eşleşir:
 * <ul>
 *   <li>{@code type}     — problemi tanımlayan URI referansı (ör. /problems/not-found)</li>
 *   <li>{@code title}    — problem tipinin kısa, insan-okur özeti</li>
 *   <li>{@code status}   — HTTP durum kodu (yanıttakiyle aynı)</li>
 *   <li>{@code detail}   — bu spesifik olaya özgü açıklama</li>
 *   <li>{@code instance} — problemin oluştuğu somut isteğin URI'si</li>
 * </ul>
 *
 * {@code @JsonInclude(NON_NULL)} sayesinde set edilmeyen alanlar JSON gövdesinde yer almaz;
 * bu, RFC 7807'nin "yalnızca anlamlı alanları döndür" ilkesiyle uyumludur.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProblemDetail {

    private URI type;
    private String title;
    private int status;
    private String detail;
    private URI instance;

    protected ProblemDetail() {
        // Jackson / alt sınıflar için
    }

    public ProblemDetail(URI type, String title, int status, String detail, URI instance) {
        this.type = type;
        this.title = title;
        this.status = status;
        this.detail = detail;
        this.instance = instance;
    }

    public URI getType() { return type; }

    public String getTitle() { return title; }

    public int getStatus() { return status; }

    public String getDetail() { return detail; }

    public URI getInstance() { return instance; }
}
