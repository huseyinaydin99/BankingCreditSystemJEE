package tr.com.huseyinaydin.application.validation.constraints;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TurkishTaxNumberValidator implements ConstraintValidator<TurkishTaxNumber, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true; // null is handled by @NotNull / @NotBlank
        if (!value.matches("\\d{10}")) return false;

        int[] d = new int[10];
        for (int i = 0; i < 10; i++) d[i] = value.charAt(i) - '0';

        // VKN checksum algorithm
        int checksum = 0;
        for (int i = 0; i < 9; i++) {
            int p = (d[i] + (9 - i)) % 10;
            if (p != 0) {
                p = (p * (int) Math.pow(2, 9 - i)) % 9;
                if (p == 0) p = 9;
            }
            checksum += p;
        }
        return d[9] == checksum % 10;
    }
}
