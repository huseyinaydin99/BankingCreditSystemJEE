package tr.com.huseyinaydin.web.config;

import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;

/**
 * Spring Security'nin DelegatingFilterProxy'sini ("springSecurityFilterChain")
 * programmatik olarak kayıt eder. Root application context üzerinden
 * banking-infrastructure içindeki SecurityConfig bean'ini kullanır.
 */
public class SecurityWebAppInitializer extends AbstractSecurityWebApplicationInitializer {
}
