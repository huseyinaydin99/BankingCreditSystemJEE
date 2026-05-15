package tr.com.huseyinaydin.sharedkernel.exception;

public class BusinessException extends ApplicationException {

    public BusinessException(String message, String code) {
        super(message, code);
    }

    public BusinessException(String message, String code, Throwable cause) {
        super(message, code, cause);
    }
}
