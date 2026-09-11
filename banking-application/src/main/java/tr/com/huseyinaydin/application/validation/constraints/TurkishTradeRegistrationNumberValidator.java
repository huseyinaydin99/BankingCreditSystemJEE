package tr.com.huseyinaydin.application.validation.constraints;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TurkishTradeRegistrationNumberValidator
        implements ConstraintValidator<TradeRegistrationNumber, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true; 
        return value.matches("\\d{4,16}");
    }
}
