package tr.com.huseyinaydin.web.config;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterRegistration;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.DispatcherServlet;
import tr.com.huseyinaydin.web.filter.CorrelationIdFilter;
import tr.com.huseyinaydin.web.filter.CorsFilter;
import tr.com.huseyinaydin.web.servlet.HealthServlet;

import java.util.EnumSet;

public class WebAppInitializer implements WebApplicationInitializer {

    @Override
    public void onStartup(ServletContext ctx) throws ServletException {

        AnnotationConfigWebApplicationContext rootCtx = new AnnotationConfigWebApplicationContext();
        rootCtx.setConfigLocations(
                "tr.com.huseyinaydin.application.config.BankingApplicationConfig",
                "tr.com.huseyinaydin.infrastructure.config.BankingInfrastructureConfig",
                "tr.com.huseyinaydin.persistence.config.PersistenceConfig"
        );
        ctx.addListener(new ContextLoaderListener(rootCtx));

        AnnotationConfigWebApplicationContext webCtx = new AnnotationConfigWebApplicationContext();
        webCtx.register(BankingWebConfig.class);

        DispatcherServlet dispatcher = new DispatcherServlet(webCtx);
        ServletRegistration.Dynamic api = ctx.addServlet("apiServlet", dispatcher);
        api.setLoadOnStartup(1);
        api.addMapping("/api/*", "/actuator/*", "/v3/*", "/swagger-ui/*", "/swagger-ui.html", "/webjars/*");

        ctx.setInitParameter("primefaces.THEME", "saga");

        ServletRegistration.Dynamic faces = ctx.addServlet("FacesServlet", "jakarta.faces.webapp.FacesServlet");
        faces.setLoadOnStartup(2);
        faces.addMapping("*.xhtml");

        ctx.addServlet("HealthServlet", HealthServlet.class).addMapping("/health");

        CharacterEncodingFilter encodingFilter = new CharacterEncodingFilter("UTF-8", true);
        FilterRegistration.Dynamic encoding = ctx.addFilter("encodingFilter", encodingFilter);
        encoding.addMappingForUrlPatterns(
                EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.INCLUDE),
                false, "/*");

        FilterRegistration.Dynamic correlation =
                ctx.addFilter("correlationIdFilter", new CorrelationIdFilter());
        correlation.addMappingForUrlPatterns(
                EnumSet.of(DispatcherType.REQUEST, DispatcherType.ASYNC, DispatcherType.ERROR),
                false, "/*");

        FilterRegistration.Dynamic cors = ctx.addFilter("corsFilter", CorsFilter.class);
        cors.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, "/api/*");

        FilterRegistration.Dynamic security = ctx.addFilter("springSecurityFilterChain", new DelegatingFilterProxy("springSecurityFilterChain"));
        security.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST, DispatcherType.ASYNC, DispatcherType.ERROR), false, "/*");

        FilterRegistration.Dynamic rateLimit =
                ctx.addFilter("rateLimitFilter", new tr.com.huseyinaydin.web.filter.RateLimitFilter(rootCtx.getEnvironment()));
        rateLimit.addMappingForUrlPatterns(
                EnumSet.of(DispatcherType.REQUEST),
                false, "/api/*");
    }
}
