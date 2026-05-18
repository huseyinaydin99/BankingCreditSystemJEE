package tr.com.huseyinaydin.application.validation.constraints;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PhoneNumberValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface PhoneNumber {
    String message() default "{validation.phoneNumber.invalid}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
