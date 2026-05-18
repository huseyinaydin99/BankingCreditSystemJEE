package tr.com.huseyinaydin.application.pipeline.behavior;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.Order;
import tr.com.huseyinaydin.application.cqrs.IPipelineBehavior;
import tr.com.huseyinaydin.application.cqrs.PipelineDelegate;
import tr.com.huseyinaydin.application.pipeline.IValidator;
import tr.com.huseyinaydin.application.pipeline.ValidationResult;
import tr.com.huseyinaydin.sharedkernel.exception.ValidationError;
import tr.com.huseyinaydin.sharedkernel.exception.ValidationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Order(1)
public class ValidationBehavior<TRequest, TResponse> implements IPipelineBehavior<TRequest, TResponse> {

    private final ApplicationContext context;
    private final Validator beanValidator;

    public ValidationBehavior(ApplicationContext context, Validator beanValidator) {
        this.context = context;
        this.beanValidator = beanValidator;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public TResponse handle(TRequest request, PipelineDelegate<TResponse> next) {
        List<ValidationError> allErrors = new ArrayList<>();

        // Layer 1: Jakarta Bean Validation (annotation-driven)
        if (beanValidator != null) {
            Set<ConstraintViolation<TRequest>> violations = beanValidator.validate(request);
            for (ConstraintViolation<TRequest> v : violations) {
                String field = v.getPropertyPath().toString();
                allErrors.add(new ValidationError(field, v.getMessage(), v.getInvalidValue()));
            }
        }

        // Layer 2: IValidator implementations (imperative rules)
        Map<String, IValidator> allValidators = context.getBeansOfType(IValidator.class);
        for (IValidator validator : allValidators.values()) {
            ResolvableType validatorType = ResolvableType.forClass(validator.getClass())
                    .as(IValidator.class);
            Class<?> validatedType = validatorType.getGeneric(0).resolve();
            if (validatedType != null && validatedType.isAssignableFrom(request.getClass())) {
                ValidationResult result = validator.validate(request);
                if (!result.isValid()) {
                    allErrors.addAll(result.getErrors());
                }
            }
        }

        if (!allErrors.isEmpty()) {
            throw new ValidationException(allErrors);
        }

        return next.proceed();
    }
}
