package tr.com.huseyinaydin.infrastructure.security;

import io.jsonwebtoken.Claims;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import tr.com.huseyinaydin.application.pipeline.ICurrentUserService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SpringCurrentUserService implements ICurrentUserService {

    private Authentication getAuth() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private Claims getClaims() {
        Authentication auth = getAuth();
        if (auth != null && auth.getDetails() instanceof Claims) {
            return (Claims) auth.getDetails();
        }
        return null;
    }

    @Override
    public String getCurrentUserId() {
        Claims claims = getClaims();
        if (claims != null) {
            String sub = claims.getSubject();
            return sub != null ? sub : claims.get(JwtClaimKeys.USER_ID, String.class);
        }
        
        Authentication auth = getAuth();
        if (auth == null || !auth.isAuthenticated()) return null;
        return auth.getName();
    }

    @Override
    public String getCurrentUserEmail() {
        Claims claims = getClaims();
        if (claims != null) {
            return claims.get(JwtClaimKeys.EMAIL, String.class);
        }
        return null;
    }

    @Override
    public Set<String> getCurrentUserRoles() {
        Authentication auth = getAuth();
        if (auth == null) return Collections.emptySet();
        
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean hasRole(String role) {
        if (role == null) return false;
        return getCurrentUserRoles().contains(role.toUpperCase());
    }

    @Override
    public boolean hasAnyRole(String... roles) {
        if (roles == null || roles.length == 0) return false;
        Set<String> userRoles = getCurrentUserRoles();
        return Arrays.stream(roles)
                .map(String::toUpperCase)
                .anyMatch(userRoles::contains);
    }

    @Override
    public boolean isAuthenticated() {
        Authentication auth = getAuth();
        return auth != null
                && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getPrincipal());
    }
}
