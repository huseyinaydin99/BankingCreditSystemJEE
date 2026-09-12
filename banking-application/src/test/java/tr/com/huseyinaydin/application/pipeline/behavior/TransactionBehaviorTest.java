package tr.com.huseyinaydin.application.pipeline.behavior;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import tr.com.huseyinaydin.application.cqrs.PipelineDelegate;
import tr.com.huseyinaydin.application.pipeline.ITransactionalRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionBehaviorTest {

    @Mock
    private PlatformTransactionManager transactionManager;

    @Mock
    private TransactionStatus transactionStatus;

    @Mock
    private PipelineDelegate<String> next;

    private TransactionBehavior<Object, String> transactionBehavior;

    static class NonTransactionalCommand {}

    static class TransactionalCommand implements ITransactionalRequest {}

    @BeforeEach
    void setUp() {
        transactionBehavior = new TransactionBehavior<>(transactionManager);
    }

    @Test
    @DisplayName("ITransactionalRequest olmayan command icin transaction baslamamali")
    void shouldNotStartTransactionForNonTransactionalRequest() {
        NonTransactionalCommand command = new NonTransactionalCommand();
        given(next.proceed()).willReturn("SUCCESS");

        String result = transactionBehavior.handle(command, next);

        assertThat(result).isEqualTo("SUCCESS");
        verify(transactionManager, never()).getTransaction(any());
    }

    @Test
    @DisplayName("Basarili islemde commit yapilmali")
    void shouldCommitOnSuccess() {
        TransactionalCommand command = new TransactionalCommand();
        given(transactionManager.getTransaction(any())).willReturn(transactionStatus);
        given(next.proceed()).willReturn("SUCCESS");
        // TransactionTemplate automatically commits if no exception is thrown
        
        String result = transactionBehavior.handle(command, next);

        assertThat(result).isEqualTo("SUCCESS");
        verify(transactionManager, times(1)).getTransaction(any());
        verify(transactionManager, times(1)).commit(transactionStatus);
        verify(transactionManager, never()).rollback(transactionStatus);
    }

    @Test
    @DisplayName("Hata durumunda rollback yapilmali")
    void shouldRollbackOnError() {
        TransactionalCommand command = new TransactionalCommand();
        given(transactionManager.getTransaction(any())).willReturn(transactionStatus);
        given(next.proceed()).willThrow(new RuntimeException("Test Hata"));
        
        // When status.setRollbackOnly() is called, TransactionTemplate rolls back
        
        assertThatThrownBy(() -> transactionBehavior.handle(command, next))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Test Hata");

        verify(transactionManager, times(1)).getTransaction(any());
        verify(transactionStatus, times(1)).setRollbackOnly();
        verify(transactionManager, times(1)).rollback(transactionStatus);
        verify(transactionManager, never()).commit(transactionStatus);
    }
}
