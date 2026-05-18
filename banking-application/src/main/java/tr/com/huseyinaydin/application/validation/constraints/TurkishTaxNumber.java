package tr.com.huseyinaydin.application.validation.constraints;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = TurkishTaxNumberValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface TurkishTaxNumber {
    String message() default "{validation.turkishTaxNumber.invalid}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
