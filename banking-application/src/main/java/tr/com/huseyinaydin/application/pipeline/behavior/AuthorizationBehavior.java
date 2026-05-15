package tr.com.huseyinaydin.application.pipeline.behavior;

import org.springframework.core.annotation.Order;
import tr.com.huseyinaydin.application.cqrs.IPipelineBehavior;
import tr.com.huseyinaydin.application.cqrs.PipelineDelegate;
import tr.com.huseyinaydin.application.pipeline.ICurrentUserService;
import tr.com.huseyinaydin.application.pipeline.ISecuredRequest;
import tr.com.huseyinaydin.sharedkernel.exception.AuthorizationException;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Order(2)
public class AuthorizationBehavior<TRequest, TResponse> implements IPipelineBehavior<TRequest, TResponse> {

    private final ICurrentUserService currentUserService;

    public AuthorizationBehavior(ICurrentUserService currentUserService) {
        this.currentUserService = currentUserService;
    }

    @Override
    public TResponse handle(TRequest request, PipelineDelegate<TResponse> next) {
        if (!(request instanceof ISecuredRequest securedRequest)) {
            return next.proceed();
        }

        if (!currentUserService.isAuthenticated()) {
            throw new AuthorizationException(
                    request.getClass().getSimpleName(), "Bu işlem için kimlik doğrulama gereklidir");
        }

        String[] requiredRoles = securedRequest.getRequiredRoles();
        if (requiredRoles == null || requiredRoles.length == 0) {
            return next.proceed();
        }

        Set<String> userRoles = Arrays.stream(currentUserService.getCurrentUserRoles())
                .map(String::toUpperCase)
                .collect(Collectors.toSet());

        boolean authorized = Arrays.stream(requiredRoles)
                .map(String::toUpperCase)
                .anyMatch(userRoles::contains);

        if (!authorized) {
            throw new AuthorizationException(
                    request.getClass().getSimpleName(),
                    "Bu işlem için gerekli rol eksik. Gerekli roller: "
                            + Arrays.toString(requiredRoles));
        }

        return next.proceed();
    }
}
