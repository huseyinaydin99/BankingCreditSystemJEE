package tr.com.huseyinaydin.application.pipeline.behavior;

import org.springframework.core.annotation.Order;
import tr.com.huseyinaydin.application.cqrs.IPipelineBehavior;
import tr.com.huseyinaydin.application.cqrs.PipelineDelegate;
import tr.com.huseyinaydin.application.pipeline.ICurrentUserService;
import tr.com.huseyinaydin.application.pipeline.ISecuredRequest;
import tr.com.huseyinaydin.sharedkernel.exception.AuthorizationException;

import java.util.Arrays;

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
        String[] requiredClaims = securedRequest.claims();

        boolean hasRequiredRoles = requiredRoles != null && requiredRoles.length > 0;
        boolean hasRequiredClaims = requiredClaims != null && requiredClaims.length > 0;

        if (!hasRequiredRoles && !hasRequiredClaims) {
            return next.proceed();
        }

        boolean roleAuthorized = hasRequiredRoles && currentUserService.hasAnyRole(requiredRoles);
        boolean claimAuthorized = hasRequiredClaims && currentUserService.hasAnyClaim(requiredClaims);

        if (!roleAuthorized && !claimAuthorized) {
            StringBuilder message = new StringBuilder("Bu işlem için gerekli yetki eksik.");
            if (hasRequiredRoles) {
                message.append(" Gerekli roller: ").append(Arrays.toString(requiredRoles));
            }
            if (hasRequiredClaims) {
                message.append(" Gerekli claimler: ").append(Arrays.toString(requiredClaims));
            }
            throw new AuthorizationException(request.getClass().getSimpleName(), message.toString());
        }

        return next.proceed();
    }
}
