package tr.com.huseyinaydin.application.pipeline.behavior;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tr.com.huseyinaydin.application.cqrs.PipelineDelegate;
import tr.com.huseyinaydin.application.pipeline.ICurrentUserService;
import tr.com.huseyinaydin.application.pipeline.ISecuredRequest;
import tr.com.huseyinaydin.sharedkernel.exception.AuthorizationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorizationBehaviorTest {

    @Mock
    private ICurrentUserService currentUserService;

    @Mock
    private PipelineDelegate<String> next;

    private AuthorizationBehavior<Object, String> authorizationBehavior;

    static class UnsecuredCommand {}

    static class SecuredCommand implements ISecuredRequest {
        @Override
        public String[] getRequiredRoles() {
            return new String[]{"ADMIN", "OFFICER"};
        }
    }

    static class EmptySecuredCommand implements ISecuredRequest {
        @Override
        public String[] getRequiredRoles() {
            return new String[]{};
        }
    }

    @BeforeEach
    void setUp() {
        authorizationBehavior = new AuthorizationBehavior<>(currentUserService);
    }

    @Test
    @DisplayName("ISecuredRequest olmayan command dogrudan gecmeli")
    void shouldDelegateWhenNotSecuredRequest() {
        UnsecuredCommand command = new UnsecuredCommand();
        given(next.proceed()).willReturn("SUCCESS");

        String result = authorizationBehavior.handle(command, next);

        assertThat(result).isEqualTo("SUCCESS");
        verify(currentUserService, never()).isAuthenticated();
    }

    @Test
    @DisplayName("Giris yapmamis kullanici icin AuthorizationException firlatilmali")
    void shouldThrowWhenNotAuthenticated() {
        SecuredCommand command = new SecuredCommand();
        given(currentUserService.isAuthenticated()).willReturn(false);

        assertThatThrownBy(() -> authorizationBehavior.handle(command, next))
                .isInstanceOf(AuthorizationException.class)
                .hasMessageContaining("kimlik doğrulama gereklidir");
        
        verify(next, never()).proceed();
    }

    @Test
    @DisplayName("Rol gerektirmeyen ISecuredRequest dogrudan gecmeli")
    void shouldDelegateWhenNoRolesRequired() {
        EmptySecuredCommand command = new EmptySecuredCommand();
        given(currentUserService.isAuthenticated()).willReturn(true);
        given(next.proceed()).willReturn("SUCCESS");

        String result = authorizationBehavior.handle(command, next);

        assertThat(result).isEqualTo("SUCCESS");
        verify(currentUserService, never()).getCurrentUserRoles();
    }

    @Test
    @DisplayName("Yetkili role sahip kullanici icin islem gecmeli")
    void shouldDelegateWhenUserHasRequiredRole() {
        SecuredCommand command = new SecuredCommand();
        given(currentUserService.isAuthenticated()).willReturn(true);
        given(currentUserService.getCurrentUserRoles()).willReturn(new String[]{"CUSTOMER", "ADMIN"});
        given(next.proceed()).willReturn("SUCCESS");

        String result = authorizationBehavior.handle(command, next);

        assertThat(result).isEqualTo("SUCCESS");
        verify(next, times(1)).proceed();
    }

    @Test
    @DisplayName("Yetkisiz role sahip kullanici icin AuthorizationException firlatilmali")
    void shouldThrowWhenUserLacksRequiredRole() {
        SecuredCommand command = new SecuredCommand();
        given(currentUserService.isAuthenticated()).willReturn(true);
        given(currentUserService.getCurrentUserRoles()).willReturn(new String[]{"CUSTOMER"});

        assertThatThrownBy(() -> authorizationBehavior.handle(command, next))
                .isInstanceOf(AuthorizationException.class)
                .hasMessageContaining("gerekli rol eksik");
        
        verify(next, never()).proceed();
    }
}
