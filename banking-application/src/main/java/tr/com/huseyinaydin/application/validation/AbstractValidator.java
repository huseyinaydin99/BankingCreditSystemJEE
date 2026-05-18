package tr.com.huseyinaydin.application.validation;

import tr.com.huseyinaydin.application.pipeline.IValidator;
import tr.com.huseyinaydin.application.pipeline.ValidationResult;
import tr.com.huseyinaydin.sharedkernel.exception.ValidationError;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class AbstractValidator<TRequest> implements IValidator<TRequest> {

    private record Rule<T>(
            Predicate<T> condition,
            String field,
            String message,
            Function<T, Object> rejectedValueExtractor
    ) {}

    private final List<Rule<TRequest>> rules = new ArrayList<>();

    protected void addRule(Predicate<TRequest> condition, String field, String message) {
        rules.add(new Rule<>(condition, field, message, null));
    }

    protected void addRule(Predicate<TRequest> condition, String field, String message,
                           Object rejectedValue) {
        rules.add(new Rule<>(condition, field, message, req -> rejectedValue));
    }

    protected void addRule(Predicate<TRequest> condition, String field, String message,
                           Function<TRequest, Object> rejectedValueExtractor) {
        rules.add(new Rule<>(condition, field, message, rejectedValueExtractor));
    }

    @Override
    public ValidationResult validate(TRequest request) {
        List<ValidationError> errors = new ArrayList<>();
        for (Rule<TRequest> rule : rules) {
            if (rule.condition().test(request)) {
                Object rejected = rule.rejectedValueExtractor() != null
                        ? rule.rejectedValueExtractor().apply(request)
                        : null;
                errors.add(new ValidationError(rule.field(), rule.message(), rejected));
            }
        }
        return errors.isEmpty() ? ValidationResult.success() : ValidationResult.failure(errors);
    }
}
