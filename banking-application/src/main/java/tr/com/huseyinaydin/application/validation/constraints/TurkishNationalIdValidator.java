package tr.com.huseyinaydin.application.validation.constraints;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TurkishNationalIdValidator implements ConstraintValidator<TurkishNationalId, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true; // null is handled by @NotNull / @NotBlank
        if (!value.matches("\\d{11}")) return false;
        if (value.charAt(0) == '0') return false;

        int[] d = new int[11];
        for (int i = 0; i < 11; i++) d[i] = value.charAt(i) - '0';

        // 10th digit check: (sum_of_odd_positions * 7 - sum_of_even_positions) % 10
        int oddSum  = d[0] + d[2] + d[4] + d[6] + d[8];
        int evenSum = d[1] + d[3] + d[5] + d[7];
        int d10 = (oddSum * 7 - evenSum) % 10;
        if (d10 < 0) d10 += 10;
        if (d10 != d[9]) return false;

        // 11th digit check: sum of first 10 digits % 10
        int total = 0;
        for (int i = 0; i < 10; i++) total += d[i];
        return total % 10 == d[10];
    }
}
