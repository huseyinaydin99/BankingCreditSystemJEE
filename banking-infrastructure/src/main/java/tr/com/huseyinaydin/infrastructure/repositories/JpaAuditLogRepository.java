package tr.com.huseyinaydin.infrastructure.repositories;

import org.springframework.stereotype.Repository;
import jakarta.persistence.EntityManager;
import tr.com.huseyinaydin.domain.audit.AuditLog;
import tr.com.huseyinaydin.domain.repositories.IAuditLogRepository;

import java.time.Instant;
import java.util.UUID;

@Repository
public class JpaAuditLogRepository extends JpaRepositoryBase<AuditLog, UUID> implements IAuditLogRepository {

    public JpaAuditLogRepository(EntityManager entityManager) {
        super(entityManager, AuditLog.class);
    }

    @Override
    public int hardDeleteOlderThan(Instant date) {
        return entityManager.createQuery("DELETE FROM AuditLog a WHERE a.createdDate < :olderThan")
                .setParameter("olderThan", date)
                .executeUpdate();
    }
}
