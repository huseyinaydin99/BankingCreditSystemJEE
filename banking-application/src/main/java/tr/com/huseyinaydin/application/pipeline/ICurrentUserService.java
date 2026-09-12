package tr.com.huseyinaydin.application.pipeline;

import java.util.Set;

public interface ICurrentUserService {
    String getCurrentUserId();
    String getCurrentUserEmail();
    Set<String> getCurrentUserRoles();
    boolean hasRole(String role);
    boolean hasAnyRole(String... roles);
    boolean isAuthenticated();
}
