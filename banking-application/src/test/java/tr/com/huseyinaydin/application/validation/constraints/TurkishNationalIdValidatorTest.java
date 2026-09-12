package tr.com.huseyinaydin.application.validation.constraints;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class TurkishNationalIdValidatorTest {

    private TurkishNationalIdValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new TurkishNationalIdValidator();
        context = mock(ConstraintValidatorContext.class);
    }

    @ParameterizedTest
    @CsvSource({
            "10000000146, true",
            "11111111110, true",
            "01000000146, false", // Starts with 0
            "1000000014, false", // Short
            "100000001461, false", // Long
            "10000000147, false", // Wrong checksum 10th digit
            "10000000141, false", // Wrong checksum 11th digit
            "1000000014A, false", // Contains letters
            ", true" // Null is handled by @NotNull
    })
    void testNationalIdValidation(String nationalId, boolean expected) {
        boolean result = validator.isValid(nationalId, context);
        assertThat(result).isEqualTo(expected);
    }
}
