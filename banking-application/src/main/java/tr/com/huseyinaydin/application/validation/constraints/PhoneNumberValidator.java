package tr.com.huseyinaydin.application.validation.constraints;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class PhoneNumberValidator implements ConstraintValidator<PhoneNumber, String> {

    // Turkish mobile number: +90 followed by 5XX XXX XXXX (10 digits starting with 5)
    private static final Pattern PATTERN = Pattern.compile("^\\+90[5][0-9]{9}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true;
        return PATTERN.matcher(value).matches();
    }
}
