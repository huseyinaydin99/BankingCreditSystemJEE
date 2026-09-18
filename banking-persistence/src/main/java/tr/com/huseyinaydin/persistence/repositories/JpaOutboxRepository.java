package tr.com.huseyinaydin.persistence.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;
import tr.com.huseyinaydin.application.ports.IOutboxRepository;
import tr.com.huseyinaydin.domain.outbox.DomainEventOutbox;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public class JpaOutboxRepository implements IOutboxRepository {

    private final EntityManager entityManager;

    public JpaOutboxRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void save(DomainEventOutbox outbox) {
        entityManager.persist(outbox);
    }

    @Override
    public List<DomainEventOutbox> findUnprocessed() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<DomainEventOutbox> query = cb.createQuery(DomainEventOutbox.class);
        Root<DomainEventOutbox> root = query.from(DomainEventOutbox.class);

        query.where(
                cb.and(
                        cb.isNull(root.get("processedAt")),
                        cb.lessThan(root.get("retryCount"), 5)
                )
        );
        // Maybe order by occurredAt?
        query.orderBy(cb.asc(root.get("occurredAt")));

        return entityManager.createQuery(query).getResultList();
    }

    @Override
    public void markProcessed(UUID id) {
        DomainEventOutbox outbox = entityManager.find(DomainEventOutbox.class, id);
        if (outbox != null) {
            outbox.markProcessed(Instant.now());
            entityManager.merge(outbox);
        }
    }

    @Override
    public void incrementRetry(UUID id, String error) {
        DomainEventOutbox outbox = entityManager.find(DomainEventOutbox.class, id);
        if (outbox != null) {
            outbox.incrementRetry(error);
            entityManager.merge(outbox);
        }
    }
}
