package tr.com.huseyinaydin.sharedkernel.exception;

public abstract class ApplicationException extends RuntimeException {

    private final String errorCode;

    protected ApplicationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    protected ApplicationException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() { return errorCode; }
}
