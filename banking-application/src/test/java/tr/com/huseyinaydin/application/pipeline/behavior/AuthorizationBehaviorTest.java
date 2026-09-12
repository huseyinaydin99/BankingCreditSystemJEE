package tr.com.huseyinaydin.application.pipeline.behavior;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tr.com.huseyinaydin.application.cqrs.PipelineDelegate;
import tr.com.huseyinaydin.application.pipeline.ISecuredRequest;
import tr.com.huseyinaydin.application.pipeline.MockCurrentUserService;
import tr.com.huseyinaydin.sharedkernel.exception.AuthorizationException;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorizationBehaviorTest {

    private MockCurrentUserService currentUserService;

    @Mock
    private PipelineDelegate<String> next;

    private AuthorizationBehavior<Object, String> authorizationBehavior;

    static class UnsecuredCommand {}

    static class SecuredRoleCommand implements ISecuredRequest {
        @Override
        public String[] getRequiredRoles() {
            return new String[]{"ADMIN", "OFFICER"};
        }
    }

    static class SecuredClaimCommand implements ISecuredRequest {
        @Override
        public String[] claims() {
            return new String[]{"individual-customers:create"};
        }
    }

    static class EmptySecuredCommand implements ISecuredRequest {}

    @BeforeEach
    void setUp() {
        currentUserService = new MockCurrentUserService();
        authorizationBehavior = new AuthorizationBehavior<>(currentUserService);
    }

    @Test
    @DisplayName("ISecuredRequest olmayan command dogrudan gecmeli")
    void shouldDelegateWhenNotSecuredRequest() {
        UnsecuredCommand command = new UnsecuredCommand();
        given(next.proceed()).willReturn("SUCCESS");

        String result = authorizationBehavior.handle(command, next);

        assertThat(result).isEqualTo("SUCCESS");
    }

    @Test
    @DisplayName("Giris yapmamis kullanici icin AuthorizationException firlatilmali")
    void shouldThrowWhenNotAuthenticated() {
        SecuredRoleCommand command = new SecuredRoleCommand();
        currentUserService.setAuthenticated(false);

        assertThatThrownBy(() -> authorizationBehavior.handle(command, next))
                .isInstanceOf(AuthorizationException.class)
                .hasMessageContaining("kimlik doğrulama gereklidir");
        
        verify(next, never()).proceed();
    }

    @Test
    @DisplayName("Rol veya claim gerektirmeyen ISecuredRequest dogrudan gecmeli")
    void shouldDelegateWhenNoRolesOrClaimsRequired() {
        EmptySecuredCommand command = new EmptySecuredCommand();
        currentUserService.setAuthenticated(true);
        given(next.proceed()).willReturn("SUCCESS");

        String result = authorizationBehavior.handle(command, next);

        assertThat(result).isEqualTo("SUCCESS");
    }

    @Test
    @DisplayName("Yetkili role sahip kullanici icin islem gecmeli")
    void shouldDelegateWhenUserHasRequiredRole() {
        SecuredRoleCommand command = new SecuredRoleCommand();
        currentUserService.setAuthenticated(true);
        currentUserService.setRoles(Set.of("CUSTOMER", "ADMIN"));
        given(next.proceed()).willReturn("SUCCESS");

        String result = authorizationBehavior.handle(command, next);

        assertThat(result).isEqualTo("SUCCESS");
        verify(next, times(1)).proceed();
    }

    @Test
    @DisplayName("Yetkili claim'e sahip kullanici icin islem gecmeli")
    void shouldDelegateWhenUserHasRequiredClaim() {
        SecuredClaimCommand command = new SecuredClaimCommand();
        currentUserService.setAuthenticated(true);
        currentUserService.setClaims(Set.of("individual-customers:create"));
        given(next.proceed()).willReturn("SUCCESS");

        String result = authorizationBehavior.handle(command, next);

        assertThat(result).isEqualTo("SUCCESS");
        verify(next, times(1)).proceed();
    }

    @Test
    @DisplayName("Yetkisiz kullanici icin AuthorizationException firlatilmali")
    void shouldThrowWhenUserLacksRequiredRoleAndClaim() {
        SecuredRoleCommand command = new SecuredRoleCommand();
        currentUserService.setAuthenticated(true);
        currentUserService.setRoles(Set.of("CUSTOMER"));

        assertThatThrownBy(() -> authorizationBehavior.handle(command, next))
                .isInstanceOf(AuthorizationException.class)
                .hasMessageContaining("gerekli yetki eksik");
        
        verify(next, never()).proceed();
    }
}
