package tr.com.huseyinaydin.application.pipeline;

public interface IValidator<TRequest> {
    ValidationResult validate(TRequest request);
}
