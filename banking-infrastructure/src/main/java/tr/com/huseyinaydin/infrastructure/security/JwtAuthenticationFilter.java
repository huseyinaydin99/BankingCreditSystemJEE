package tr.com.huseyinaydin.infrastructure.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import tr.com.huseyinaydin.application.ports.IJwtService;
import tr.com.huseyinaydin.sharedkernel.exception.ApplicationException;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ROLES_CLAIM = "roles";

    private final IJwtService jwtService;

    public JwtAuthenticationFilter(IJwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(AUTH_HEADER);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(BEARER_PREFIX.length());
        try {
            Claims claims = jwtService.validateToken(token);

            List<?> rawRoles = claims.get(ROLES_CLAIM, List.class);
            List<SimpleGrantedAuthority> authorities = rawRoles == null
                    ? List.of()
                    : rawRoles.stream()
                            .map(r -> new SimpleGrantedAuthority("ROLE_" + r.toString().toUpperCase()))
                            .collect(Collectors.toList());

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(claims.getSubject(), null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (ApplicationException ignored) {
            // Geçersiz/süresi dolmuş token — anonim devam; güvenlik filtresi reddeder
        }

        filterChain.doFilter(request, response);
    }
}
