package tr.com.huseyinaydin.infrastructure.audit;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.springframework.stereotype.Service;
import tr.com.huseyinaydin.application.ports.IAuditService;
import tr.com.huseyinaydin.domain.audit.AuditLog;
import tr.com.huseyinaydin.sharedkernel.audit.AuditEntry;

/**
 * {@link IAuditService} JPA implementasyonu. Denetim kaydı, asıl komut transaction'ı commit
 * olduktan SONRA (AuditBehavior, @Order(6)) yazıldığından, kendi kısa ömürlü EntityManager'ı
 * ve transaction'ı ile bağımsız olarak kalıcılaştırılır (JpaUnitOfWork ile aynı yaklaşım).
 */
@Service
public class JpaAuditService implements IAuditService {

    private final EntityManagerFactory entityManagerFactory;

    public JpaAuditService(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public void record(AuditEntry entry) {
        AuditLog auditLog = new AuditLog(
                entry.entityId(),
                entry.entityType(),
                entry.action(),
                entry.performedBy(),
                entry.performedAt(),
                entry.ipAddress(),
                entry.oldValue(),
                entry.newValue());

        EntityManager em = entityManagerFactory.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(auditLog);
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }
}
