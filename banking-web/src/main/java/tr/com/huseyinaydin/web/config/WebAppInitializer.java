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
import org.springframework.web.servlet.DispatcherServlet;
import tr.com.huseyinaydin.web.filter.CorsFilter;
import tr.com.huseyinaydin.web.servlet.HealthServlet;

import java.util.EnumSet;

public class WebAppInitializer implements WebApplicationInitializer {

    @Override
    public void onStartup(ServletContext ctx) throws ServletException {

        // Root application context — loads application, infrastructure, persistence layers
        // Using string-based config to avoid compile-time dependency on runtime-scope modules
        AnnotationConfigWebApplicationContext rootCtx = new AnnotationConfigWebApplicationContext();
        rootCtx.setConfigLocations(
                "tr.com.huseyinaydin.application.config.BankingApplicationConfig",
                "tr.com.huseyinaydin.infrastructure.config.BankingInfrastructureConfig",
                "tr.com.huseyinaydin.persistence.config.PersistenceConfig"
        );
        ctx.addListener(new ContextLoaderListener(rootCtx));

        // DispatcherServlet context — Spring MVC web layer only
        AnnotationConfigWebApplicationContext webCtx = new AnnotationConfigWebApplicationContext();
        webCtx.register(BankingWebConfig.class);

        DispatcherServlet dispatcher = new DispatcherServlet(webCtx);
        ServletRegistration.Dynamic api = ctx.addServlet("apiServlet", dispatcher);
        api.setLoadOnStartup(1);
        api.addMapping("/api/*");

        // PrimeFaces tema ayarı (saga: açık mavi tema)
        ctx.setInitParameter("primefaces.THEME", "saga");

        // JSF/Facelets servlet
        ServletRegistration.Dynamic faces = ctx.addServlet("FacesServlet", "jakarta.faces.webapp.FacesServlet");
        faces.setLoadOnStartup(2);
        faces.addMapping("*.xhtml");

        // Health check servlet
        ctx.addServlet("HealthServlet", HealthServlet.class).addMapping("/health");

        // UTF-8 encoding filter for all requests
        CharacterEncodingFilter encodingFilter = new CharacterEncodingFilter("UTF-8", true);
        FilterRegistration.Dynamic encoding = ctx.addFilter("encodingFilter", encodingFilter);
        encoding.addMappingForUrlPatterns(
                EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.INCLUDE),
                false, "/*");

        // CORS filter for REST API
        FilterRegistration.Dynamic cors = ctx.addFilter("corsFilter", CorsFilter.class);
        cors.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, "/api/*");
    }
}
