package tr.com.huseyinaydin.application.pipeline;

import java.util.Set;

public interface ICurrentUserService {
    String getCurrentUserId();
    String getCurrentUserEmail();
    Set<String> getCurrentUserRoles();
    Set<String> getCurrentUserClaims();
    boolean hasRole(String role);
    boolean hasAnyRole(String... roles);
    boolean hasClaim(String claim);
    boolean hasAnyClaim(String... claims);
    boolean isAuthenticated();
}
