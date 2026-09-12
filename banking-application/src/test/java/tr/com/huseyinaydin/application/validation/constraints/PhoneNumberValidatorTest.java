package tr.com.huseyinaydin.application.validation.constraints;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class PhoneNumberValidatorTest {

    private PhoneNumberValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new PhoneNumberValidator();
        context = mock(ConstraintValidatorContext.class);
    }

    @ParameterizedTest
    @CsvSource({
            "'+905551234567', true",
            "'+905001234567', true",
            "'+904551234567', false", // Doesn't start with 5 after +90
            "'+90555123456', false", // Short
            "'+9055512345678', false", // Long
            "05551234567, false", // Missing +90
            "5551234567, false", // Missing +90
            ", true" // Null is handled by @NotNull
    })
    void testPhoneNumberValidation(String phoneNumber, boolean expected) {
        boolean result = validator.isValid(phoneNumber, context);
        assertThat(result).isEqualTo(expected);
    }
}
