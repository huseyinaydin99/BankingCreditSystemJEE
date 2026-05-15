package tr.com.huseyinaydin.sharedkernel.result;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public final class Result<T> {

    private final T value;
    private final boolean success;
    private final List<BusinessError> errors;

    private Result(T value, boolean success, List<BusinessError> errors) {
        this.value = value;
        this.success = success;
        this.errors = errors != null ? List.copyOf(errors) : Collections.emptyList();
    }

    public static <T> Result<T> success(T value) {
        return new Result<>(value, true, Collections.emptyList());
    }

    public static Result<Void> ok() {
        return new Result<>(null, true, Collections.emptyList());
    }

    public static <T> Result<T> failure(BusinessError error) {
        return new Result<>(null, false, Collections.singletonList(error));
    }

    public static <T> Result<T> failure(List<BusinessError> errors) {
        return new Result<>(null, false, errors);
    }

    public static Result<Void> fail(BusinessError error) {
        return failure(error);
    }

    public static Result<Void> fail(List<BusinessError> errors) {
        return failure(errors);
    }

    public boolean isSuccess() { return success; }
    public boolean isFailure() { return !success; }

    public T getValue() {
        if (!success) {
            throw new IllegalStateException("Başarısız Result'tan değer alınamaz. Hata: "
                    + (errors.isEmpty() ? "bilinmiyor" : errors.get(0).message()));
        }
        return value;
    }

    public List<BusinessError> getErrors() { return errors; }

    public <R> Result<R> map(Function<T, R> mapper) {
        if (isFailure()) return failure(errors);
        return success(mapper.apply(value));
    }

    public <R> Result<R> flatMap(Function<T, Result<R>> mapper) {
        if (isFailure()) return failure(errors);
        return mapper.apply(value);
    }

    public Result<T> onSuccess(Consumer<T> consumer) {
        if (isSuccess()) consumer.accept(value);
        return this;
    }

    public Result<T> onFailure(Consumer<List<BusinessError>> consumer) {
        if (isFailure()) consumer.accept(errors);
        return this;
    }
}
