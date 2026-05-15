package tr.com.huseyinaydin.sharedkernel.result;

public record BusinessError(String code, String message, String field) {

    public BusinessError(String code, String message) {
        this(code, message, null);
    }
}
