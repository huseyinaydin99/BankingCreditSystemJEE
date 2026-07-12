package tr.com.huseyinaydin.application.config;

import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import tr.com.huseyinaydin.application.pipeline.ICurrentUserService;
import tr.com.huseyinaydin.application.ports.IAuditService;
import tr.com.huseyinaydin.application.ports.IpAddressProvider;
import tr.com.huseyinaydin.application.pipeline.behavior.AuditBehavior;
import tr.com.huseyinaydin.application.pipeline.behavior.AuthorizationBehavior;
import tr.com.huseyinaydin.application.pipeline.behavior.LoggingBehavior;
import tr.com.huseyinaydin.application.pipeline.behavior.PerformanceBehavior;
import tr.com.huseyinaydin.application.pipeline.behavior.TransactionBehavior;
import tr.com.huseyinaydin.application.pipeline.behavior.ValidationBehavior;

@Configuration
public class BehaviorConfig {

    @Bean
    public LocalValidatorFactoryBean localValidatorFactoryBean() {
        return new LocalValidatorFactoryBean();
    }

    @Bean
    @Order(1)
    public ValidationBehavior<?, ?> validationBehavior(
            ApplicationContext context,
            @Autowired(required = false) Validator beanValidator) {
        return new ValidationBehavior<>(context, beanValidator);
    }

    @Bean
    @Order(2)
    public AuthorizationBehavior<?, ?> authorizationBehavior(
            @Autowired(required = false) ICurrentUserService currentUserService) {
        if (currentUserService == null) {
            return new AuthorizationBehavior<>(new NoOpCurrentUserService());
        }
        return new AuthorizationBehavior<>(currentUserService);
    }

    @Bean
    @Order(3)
    public LoggingBehavior<?, ?> loggingBehavior(
            @Autowired(required = false) ICurrentUserService currentUserService) {
        return new LoggingBehavior<>(currentUserService);
    }

    @Bean
    @Order(4)
    public PerformanceBehavior<?, ?> performanceBehavior() {
        return new PerformanceBehavior<>();
    }

    @Bean
    @Order(6)
    public AuditBehavior<?, ?> auditBehavior(
            @Autowired(required = false) IAuditService auditService,
            @Autowired(required = false) IpAddressProvider ipAddressProvider,
            @Autowired(required = false) ICurrentUserService currentUserService) {
        // auditService yoksa (ör. infra bağlı değilse) behavior no-op'tur — bkz. AuditBehavior.handle
        return new AuditBehavior<>(auditService, ipAddressProvider, currentUserService);
    }

    @Bean
    @Order(10)
    public TransactionBehavior<?, ?> transactionBehavior(
            @Autowired(required = false) PlatformTransactionManager transactionManager) {
        if (transactionManager == null) {
            return new NoOpTransactionBehavior<>();
        }
        return new TransactionBehavior<>(transactionManager);
    }

    private static class NoOpCurrentUserService implements ICurrentUserService {
        @Override public String getCurrentUserId() { return "anonymous"; }
        @Override public String[] getCurrentUserRoles() { return new String[0]; }
        @Override public boolean isAuthenticated() { return false; }
    }

    private static class NoOpTransactionBehavior<TRequest, TResponse>
            extends TransactionBehavior<TRequest, TResponse> {
        NoOpTransactionBehavior() { super(null); }

        @Override
        public TResponse handle(TRequest request,
                tr.com.huseyinaydin.application.cqrs.PipelineDelegate<TResponse> next) {
            return next.proceed();
        }
    }
}
