package tr.com.huseyinaydin.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;
import tr.com.huseyinaydin.sharedkernel.logging.CorrelationId;

import java.io.IOException;


public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String HEADER_NAME = "X-Correlation-ID";
    public static final String MDC_KEY = "correlationId";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        CorrelationId correlationId = CorrelationId.of(request.getHeader(HEADER_NAME));
        String value = correlationId.asString();

        MDC.put(MDC_KEY, value);
        response.setHeader(HEADER_NAME, value);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }
}
