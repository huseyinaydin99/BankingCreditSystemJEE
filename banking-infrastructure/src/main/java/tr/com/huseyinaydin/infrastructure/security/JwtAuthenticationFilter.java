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

import io.micrometer.tracing.BaggageInScope;
import io.micrometer.tracing.Tracer;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ROLES_CLAIM = "roles";

    private final IJwtService jwtService;
    private final Tracer tracer;

    public JwtAuthenticationFilter(IJwtService jwtService, Tracer tracer) {
        this.jwtService = jwtService;
        this.tracer = tracer;
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
        String userId = null;
        try {
            Claims claims = jwtService.validateToken(token);
            userId = claims.getSubject();

            List<?> rawRoles = claims.get(ROLES_CLAIM, List.class);
            List<SimpleGrantedAuthority> authorities = rawRoles == null
                    ? List.of()
                    : rawRoles.stream()
                            .map(r -> new SimpleGrantedAuthority("ROLE_" + r.toString().toUpperCase()))
                            .collect(Collectors.toList());

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (ApplicationException ignored) {
        }

        if (tracer != null && userId != null) {
            try (BaggageInScope bag = tracer.createBaggageInScope("userId", userId)) {
                filterChain.doFilter(request, response);
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }
}
