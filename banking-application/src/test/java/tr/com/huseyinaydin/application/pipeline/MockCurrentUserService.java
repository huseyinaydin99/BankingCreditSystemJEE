package tr.com.huseyinaydin.application.pipeline;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class MockCurrentUserService implements ICurrentUserService {

    private String userId;
    private String email;
    private Set<String> roles;
    private boolean authenticated;

    public MockCurrentUserService(String userId, String email, Set<String> roles) {
        this.userId = userId;
        this.email = email;
        this.roles = roles != null ? roles : Collections.emptySet();
        this.authenticated = true;
    }

    public MockCurrentUserService() {
        this.authenticated = false;
        this.roles = Collections.emptySet();
    }

    @Override
    public String getCurrentUserId() {
        return userId;
    }

    @Override
    public String getCurrentUserEmail() {
        return email;
    }

    @Override
    public Set<String> getCurrentUserRoles() {
        return roles;
    }

    @Override
    public boolean hasRole(String role) {
        if (role == null) return false;
        return roles.contains(role.toUpperCase());
    }

    @Override
    public boolean hasAnyRole(String... rolesToCheck) {
        if (rolesToCheck == null || rolesToCheck.length == 0) return false;
        return Arrays.stream(rolesToCheck)
                .map(String::toUpperCase)
                .anyMatch(roles::contains);
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }
}
