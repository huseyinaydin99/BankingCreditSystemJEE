package tr.com.huseyinaydin.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tr.com.huseyinaydin.application.ports.IJwtService;
import tr.com.huseyinaydin.infrastructure.security.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final IJwtService jwtService;
    private final io.micrometer.tracing.Tracer tracer;

    public SecurityConfig(IJwtService jwtService, @org.springframework.beans.factory.annotation.Autowired(required = false) io.micrometer.tracing.Tracer tracer) {
        this.jwtService = jwtService;
        this.tracer = tracer;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/actuator/**").hasRole("ADMIN")
                    .requestMatchers("/health", "/api/v1/auth/**").permitAll()
                    .requestMatchers("/api/v1/credit-applications/**").hasRole("CUSTOMER")
                    .requestMatchers("/api/v1/*-customers/**", "/api/v1/credit-types/**")
                            .hasAnyRole("OFFICER", "ADMIN")
                    .anyRequest().authenticated()
            )
            .addFilterBefore(
                    new JwtAuthenticationFilter(jwtService, tracer),
                    UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}
