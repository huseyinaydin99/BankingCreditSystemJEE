package tr.com.huseyinaydin.application.pipeline;

public interface ICurrentUserService {
    String getCurrentUserId();
    String[] getCurrentUserRoles();
    boolean isAuthenticated();
}
