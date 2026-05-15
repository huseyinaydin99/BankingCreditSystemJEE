package tr.com.huseyinaydin.application.exception;

public class HandlerNotFoundException extends RuntimeException {

    public HandlerNotFoundException(Class<?> requestType) {
        super("Handler bulunamadı: " + requestType.getSimpleName()
                + ". Handler @Component ile işaretlenmiş olmalıdır.");
    }

    public HandlerNotFoundException(String message) {
        super(message);
    }
}
