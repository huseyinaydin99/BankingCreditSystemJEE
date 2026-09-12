package tr.com.huseyinaydin.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class DeprecationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String uri = request.getRequestURI();

        if (uri.startsWith("/api/") && !uri.startsWith("/api/v")) {
            String newUri = uri.replaceFirst("/api/", "/api/v1/");
            
            // Append query string if exists
            String queryString = request.getQueryString();
            if (queryString != null) {
                newUri += "?" + queryString;
            }

            response.setHeader("Deprecation", "true");
            response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
            response.setHeader("Location", newUri);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
