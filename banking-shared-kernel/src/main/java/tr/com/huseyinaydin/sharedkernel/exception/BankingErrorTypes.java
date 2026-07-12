package tr.com.huseyinaydin.sharedkernel.exception;

import java.net.URI;

/**
 * RFC 7807 {@code type} URI'lerinin ve bunlara eşlik eden varsayılan başlık/HTTP durum
 * kodlarının tek merkezden tanımlandığı katalog.
 *
 * Her sabit, bir problem sınıfını temsil eder. {@code type} değerleri istemcilerin
 * dokümantasyonda çözümleyebileceği kararlı URI referanslarıdır (RFC 7807, mutlak URI
 * gerektirmez; relatif referanslar da geçerlidir). Başlık ve durum kodunun burada
 * sabitlenmesi, GlobalExceptionHandler içindeki eşlemelerin tutarlı kalmasını sağlar.
 */
public enum BankingErrorTypes {

    BUSINESS_RULE_VIOLATION("/problems/business-rule-violation", "İş Kuralı İhlali", 400),
    VALIDATION_FAILED("/problems/validation-failed", "Doğrulama Hatası", 400),
    NOT_FOUND("/problems/not-found", "Kaynak Bulunamadı", 404),
    UNAUTHORIZED("/problems/unauthorized", "Yetkisiz İşlem", 401),
    CONFLICT("/problems/conflict", "Kaynak Çakışması", 409),
    INTERNAL_ERROR("/problems/internal-error", "Sunucu Hatası", 500);

    private final String type;
    private final String title;
    private final int status;

    BankingErrorTypes(String type, String title, int status) {
        this.type = type;
        this.title = title;
        this.status = status;
    }

    /** Problemi tanımlayan URI referansı (RFC 7807 {@code type}). */
    public URI type() { return URI.create(type); }

    /** Problem tipinin kısa, insan-okur özeti (RFC 7807 {@code title}). */
    public String title() { return title; }

    /** Bu problem tipiyle eşleşen HTTP durum kodu (RFC 7807 {@code status}). */
    public int status() { return status; }
}
