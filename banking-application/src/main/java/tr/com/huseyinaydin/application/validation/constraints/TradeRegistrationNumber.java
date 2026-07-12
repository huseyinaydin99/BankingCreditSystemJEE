package tr.com.huseyinaydin.application.validation.constraints;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Türkiye Ticaret Sicil Numarası doğrulaması. {@link TurkishTaxNumber} ile aynı yapıda;
 * ancak Ticaret Sicil / MERSİS için evrensel bir checksum bulunmadığından yalnızca
 * biçim (rakam) ve uzunluk (4-16 hane) kontrolü yapılır.
 */
@Documented
@Constraint(validatedBy = TurkishTradeRegistrationNumberValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface TradeRegistrationNumber {
    String message() default "{validation.tradeRegistrationNumber.invalid}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
