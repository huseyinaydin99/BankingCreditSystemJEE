package tr.com.huseyinaydin.web.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
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
import org.springframework.core.env.Environment;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import tr.com.huseyinaydin.application.pipeline.ICurrentUserService;
import tr.com.huseyinaydin.sharedkernel.exception.RateLimitProblemDetail;

import java.io.IOException;
import java.time.Duration;

public class RateLimitFilter extends OncePerRequestFilter {

    private final Environment env;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private HazelcastProxyManager<String> proxyManager;

    private int authLimit;
    private int anonLimit;

    public RateLimitFilter(Environment env) {
        this.env = env;
    }

    @Override
    protected void initFilterBean() throws ServletException {
        this.authLimit = env.getProperty("banking.rate-limit.credit-application.authenticated", Integer.class, 10);
        this.anonLimit = env.getProperty("banking.rate-limit.credit-application.anonymous", Integer.class, 3);

        Config config = new Config();
        config.setClusterName("banking-rate-limit-cluster");
        HazelcastInstance hazelcastInstance = Hazelcast.getOrCreateHazelcastInstance(config);

        IMap<String, byte[]> map = hazelcastInstance.getMap("rate-limits");
        this.proxyManager = new HazelcastProxyManager<>(map);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if ("POST".equalsIgnoreCase(request.getMethod()) && request.getRequestURI().matches(".*/api/(v1/)?credit-?applications.*")) {
            
            ICurrentUserService currentUserService = WebApplicationContextUtils
                    .getRequiredWebApplicationContext(request.getServletContext())
                    .getBean(ICurrentUserService.class);

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
