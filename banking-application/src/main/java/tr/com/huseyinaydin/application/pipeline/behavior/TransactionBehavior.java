package tr.com.huseyinaydin.application.pipeline.behavior;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import tr.com.huseyinaydin.application.cqrs.IPipelineBehavior;
import tr.com.huseyinaydin.application.cqrs.PipelineDelegate;
import tr.com.huseyinaydin.application.pipeline.ITransactionalRequest;

@Order(10)
public class TransactionBehavior<TRequest, TResponse> implements IPipelineBehavior<TRequest, TResponse> {

    private static final Logger log = LoggerFactory.getLogger(TransactionBehavior.class);

    private final TransactionTemplate transactionTemplate;

    public TransactionBehavior(PlatformTransactionManager transactionManager) {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        this.transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
    }

    @Override
    public TResponse handle(TRequest request, PipelineDelegate<TResponse> next) {
        if (!(request instanceof ITransactionalRequest)) {
            return next.proceed();
        }

        String requestType = request.getClass().getSimpleName();
        log.debug("[{}] transaction başlatılıyor", requestType);

        return transactionTemplate.execute(status -> {
            try {
                TResponse result = next.proceed();
                log.debug("[{}] transaction commit", requestType);
                return result;
            } catch (Exception ex) {
                status.setRollbackOnly();
                log.warn("[{}] transaction rollback — {}", requestType, ex.getMessage());
                throw ex;
            }
        });
    }
}
