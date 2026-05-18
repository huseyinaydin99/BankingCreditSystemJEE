package tr.com.huseyinaydin.application.validation.constraints;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = TurkishNationalIdValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface TurkishNationalId {
    String message() default "{validation.turkishNationalId.invalid}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
