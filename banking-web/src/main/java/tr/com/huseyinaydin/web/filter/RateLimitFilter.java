package tr.com.huseyinaydin.web.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import tr.com.huseyinaydin.sharedkernel.exception.RateLimitProblemDetail;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimitFilter extends OncePerRequestFilter {

    private final Environment env;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ConcurrentHashMap<String, Bucket> authenticatedBuckets = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Bucket> anonymousBuckets = new ConcurrentHashMap<>();

    private int authLimit;
    private int anonLimit;

    public RateLimitFilter(Environment env) {
        this.env = env;
    }

    @Override
    protected void initFilterBean() throws ServletException {
        this.authLimit = env.getProperty("banking.rate-limit.credit-application.authenticated", Integer.class, 10);
        this. anonLimit = env.getProperty("banking.rate-limit.credit-application.anonymous", Integer.class, 3);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if ("POST".equalsIgnoreCase(request.getMethod()) && request.getRequestURI().matches(".*/api/(v1/)?credit-?applications.*")) {
            
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isAuthenticated = auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser");

            Bucket bucket;
            if (isAuthenticated) {
                String username = auth.getName();
                bucket = authenticatedBuckets.computeIfAbsent(username, this::createAuthenticatedBucket);
            } else {
                String ip = getClientIP(request);
                bucket = anonymousBuckets.computeIfAbsent(ip, this::createAnonymousBucket);
            }

            ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
            if (probe.isConsumed()) {
                response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
                filterChain.doFilter(request, response);
            } else {
                long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;
                response.setStatus(429);
                response.addHeader("Retry-After", String.valueOf(waitForRefill));
                response.setContentType("application/problem+json;charset=UTF-8");
                RateLimitProblemDetail problemDetail = new RateLimitProblemDetail("Rate limit exceeded.");
                objectMapper.writeValue(response.getWriter(), problemDetail);
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }

    private Bucket createAuthenticatedBucket(String key) {
        Bandwidth limit = Bandwidth.classic(authLimit, Refill.greedy(authLimit, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    private Bucket createAnonymousBucket(String key) {
        Bandwidth limit = Bandwidth.classic(anonLimit, Refill.greedy(anonLimit, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty() || !xfHeader.contains(request.getRemoteAddr())) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
