package tr.com.huseyinaydin.application.pipeline.behavior;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import tr.com.huseyinaydin.application.cqrs.PipelineDelegate;
import tr.com.huseyinaydin.application.pipeline.IValidator;
import tr.com.huseyinaydin.application.pipeline.ValidationResult;
import tr.com.huseyinaydin.sharedkernel.exception.ValidationException;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValidationBehaviorTest {

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private Validator beanValidator;

    @Mock
    private PipelineDelegate<String> next;

    private ValidationBehavior<TestCommand, String> validationBehavior;

    static class TestCommand {
        public String name;
    }

    static class TestCommandValidator implements IValidator<TestCommand> {
        @Override
        public ValidationResult validate(TestCommand target) {
            return ValidationResult.success();
        }
    }

    @BeforeEach
    void setUp() {
        validationBehavior = new ValidationBehavior<>(applicationContext, beanValidator);
    }

    @Test
    @DisplayName("Geçerli command durumunda sonraki handler çağrılmalı")
    void shouldDelegateToNextWhenValid() {
        TestCommand command = new TestCommand();
        given(beanValidator.validate(command)).willReturn(Collections.emptySet());
        given(applicationContext.getBeansOfType(IValidator.class)).willReturn(Collections.emptyMap());
        given(next.proceed()).willReturn("SUCCESS");

        String result = validationBehavior.handle(command, next);

        assertThat(result).isEqualTo("SUCCESS");
        verify(next, times(1)).proceed();
    }

    @Test
    @DisplayName("Jakarta Bean Validation hatası olduğunda ValidationException fırlatılmalı")
    void shouldThrowValidationExceptionWhenBeanValidationFails() {
        TestCommand command = new TestCommand();
        ConstraintViolation<TestCommand> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        given(path.toString()).willReturn("name");
        given(violation.getPropertyPath()).willReturn(path);
        given(violation.getMessage()).willReturn("Bos olamaz");
        given(violation.getInvalidValue()).willReturn(null);
        
        given(beanValidator.validate(command)).willReturn(Set.of(violation));

        assertThatThrownBy(() -> validationBehavior.handle(command, next))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Doğrulama hataları oluştu");
        
        verify(next, never()).proceed();
    }

    @Test
    @DisplayName("IValidator hatası olduğunda ValidationException fırlatılmalı")
    void shouldThrowValidationExceptionWhenIValidatorFails() {
        TestCommand command = new TestCommand();
        given(beanValidator.validate(command)).willReturn(Collections.emptySet());

        IValidator<TestCommand> mockValidator = mock(IValidator.class);
        ValidationResult failedResult = ValidationResult.failure("name", "Gecersiz");
        given(mockValidator.validate(any())).willReturn(failedResult);

        // IValidator implementations have a generic signature, 
        // ValidationBehavior uses reflection, but testing the exact inner workings 
        // requires actual Spring class logic or precise mock returns.
        // For simplicity we simulate it properly.
        Map<String, IValidator> beans = Map.of("testValidator", mockValidator);
        given(applicationContext.getBeansOfType(IValidator.class)).willReturn(beans);

        // However, reflection ResolvableType.forClass(mockValidator.getClass()) might return generic object type for mocked classes
        // Let's use a real instance for the bean to make ResolvableType work
        IValidator<TestCommand> realValidator = new IValidator<TestCommand>() {
            @Override
            public ValidationResult validate(TestCommand target) {
                return ValidationResult.failure("name", "Gecersiz");
            }
        };
        Map<String, IValidator> beansReal = Map.of("testValidator", realValidator);
        given(applicationContext.getBeansOfType(IValidator.class)).willReturn(beansReal);

        assertThatThrownBy(() -> validationBehavior.handle(command, next))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Doğrulama hataları oluştu");
        
        verify(next, never()).proceed();
    }
}
