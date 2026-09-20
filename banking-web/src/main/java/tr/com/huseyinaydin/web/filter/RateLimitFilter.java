package tr.com.huseyinaydin.web.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import io.github.bucket4j.distributed.BucketProxy;
import io.github.bucket4j.grid.hazelcast.HazelcastProxyManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tr.com.huseyinaydin.application.pipeline.ICurrentUserService;
import tr.com.huseyinaydin.sharedkernel.exception.RateLimitProblemDetail;

import java.io.IOException;
import java.time.Duration;

@Component("rateLimitFilter")
public class RateLimitFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final HazelcastProxyManager<String> proxyManager;
    private final ICurrentUserService currentUserService;

    private final int authLimit;
    private final int anonLimit;

    public RateLimitFilter(HazelcastProxyManager<String> proxyManager,
                           ICurrentUserService currentUserService,
                           @Value("${banking.rate-limit.credit-application.authenticated:10}") int authLimit,
                           @Value("${banking.rate-limit.credit-application.anonymous:3}") int anonLimit) {
        this.proxyManager = proxyManager;
        this.currentUserService = currentUserService;
        this.authLimit = authLimit;
        this.anonLimit = anonLimit;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if ("POST".equalsIgnoreCase(request.getMethod())) {
            
            boolean isAuthenticated = currentUserService.isAuthenticated();
            String key;
            BucketConfiguration configuration;

            if (isAuthenticated) {
                key = "auth:" + currentUserService.getCurrentUserId();
                configuration = BucketConfiguration.builder()
                        .addLimit(Bandwidth.classic(authLimit, Refill.greedy(authLimit, Duration.ofMinutes(1))))
                        .build();
            } else {
                key = "anon:" + getClientIP(request);
                configuration = BucketConfiguration.builder()
                        .addLimit(Bandwidth.classic(anonLimit, Refill.greedy(anonLimit, Duration.ofMinutes(1))))
                        .build();
            }

            BucketProxy bucket = proxyManager.builder().build(key, configuration);
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

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty() || !xfHeader.contains(request.getRemoteAddr())) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
