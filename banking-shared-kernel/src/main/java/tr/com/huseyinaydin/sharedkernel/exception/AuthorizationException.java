package tr.com.huseyinaydin.sharedkernel.exception;

public class AuthorizationException extends ApplicationException {

    private static final String ERROR_CODE = "AUTHORIZATION_ERROR";

    private final String action;

    public AuthorizationException(String action) {
        super("Yetkisiz işlem: " + action, ERROR_CODE);
        this.action = action;
    }

    public AuthorizationException(String action, String message) {
        super(message, ERROR_CODE);
        this.action = action;
    }

    public AuthorizationException(String action, String message, Throwable cause) {
        super(message, ERROR_CODE, cause);
        this.action = action;
    }

    public String getAction() { return action; }
}
