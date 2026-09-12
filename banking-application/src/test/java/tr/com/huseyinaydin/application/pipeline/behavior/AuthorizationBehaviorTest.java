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
        SecuredCommand command = new SecuredCommand();
        currentUserService.setAuthenticated(false);

        assertThatThrownBy(() -> authorizationBehavior.handle(command, next))
                .isInstanceOf(AuthorizationException.class)
                .hasMessageContaining("kimlik doğrulama gereklidir");
        
        verify(next, never()).proceed();
    }

    @Test
    @DisplayName("Rol gerektirmeyen ISecuredRequest dogrudan gecmeli")
    void shouldDelegateWhenNoRolesRequired() {
        EmptySecuredCommand command = new EmptySecuredCommand();
        currentUserService.setAuthenticated(true);
        given(next.proceed()).willReturn("SUCCESS");

        String result = authorizationBehavior.handle(command, next);

        assertThat(result).isEqualTo("SUCCESS");
    }

    @Test
    @DisplayName("Yetkili role sahip kullanici icin islem gecmeli")
    void shouldDelegateWhenUserHasRequiredRole() {
        SecuredCommand command = new SecuredCommand();
        currentUserService.setAuthenticated(true);
        currentUserService.setRoles(Set.of("CUSTOMER", "ADMIN"));
        given(next.proceed()).willReturn("SUCCESS");

        String result = authorizationBehavior.handle(command, next);

        assertThat(result).isEqualTo("SUCCESS");
        verify(next, times(1)).proceed();
    }

    @Test
    @DisplayName("Yetkisiz role sahip kullanici icin AuthorizationException firlatilmali")
    void shouldThrowWhenUserLacksRequiredRole() {
        SecuredCommand command = new SecuredCommand();
        currentUserService.setAuthenticated(true);
        currentUserService.setRoles(Set.of("CUSTOMER"));

        assertThatThrownBy(() -> authorizationBehavior.handle(command, next))
                .isInstanceOf(AuthorizationException.class)
                .hasMessageContaining("gerekli rol eksik");
        
        verify(next, never()).proceed();
    }
}
