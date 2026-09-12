package tr.com.huseyinaydin.application.validation.constraints;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class TurkishTaxNumberValidatorTest {

    private TurkishTaxNumberValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new TurkishTaxNumberValidator();
        context = mock(ConstraintValidatorContext.class);
    }

    @ParameterizedTest
    @CsvSource({
            "0000000000, true",
            "0000000001, false", // Wrong checksum
            "123456789, false", // Short
            "12345678901, false", // Long
            "123456789A, false", // Contains letters
            ", true" // Null is handled by @NotNull
    })
    void testTaxNumberValidation(String taxNumber, boolean expected) {
        boolean result = validator.isValid(taxNumber, context);
        assertThat(result).isEqualTo(expected);
    }
}
