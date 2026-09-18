package tr.com.huseyinaydin.persistence.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import tr.com.huseyinaydin.domain.common.BaseEntity;
import tr.com.huseyinaydin.domain.repositories.IAsyncRepository;
import tr.com.huseyinaydin.domain.repositories.Specification;
import tr.com.huseyinaydin.sharedkernel.pagination.Paginate;
import tr.com.huseyinaydin.sharedkernel.pagination.PaginationRequest;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.lang.reflect.Method;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public abstract class JpaRepositoryBase<TEntity extends BaseEntity<TId>, TId>
        implements IAsyncRepository<TEntity, TId> {

    protected final EntityManager entityManager;
    protected final Class<TEntity> entityClass;
    private final ObjectMapper objectMapper;

    protected JpaRepositoryBase(EntityManager entityManager, Class<TEntity> entityClass) {
        this.entityManager = entityManager;
        this.entityClass = entityClass;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    @io.github.resilience4j.retry.annotation.Retry(name = "jpaRetry")
    public Optional<TEntity> findById(TId id) {
        TEntity entity = entityManager.find(entityClass, id);
        if (entity == null || entity.getDeletedDate() != null) {
            return Optional.empty();
        }
        return Optional.of(entity);
    }

    @Override
    public Paginate<TEntity> findAll(Specification<TEntity> spec, PaginationRequest pagination) {
        return findAll(spec, pagination, false);
    }

    @Override
    public Paginate<TEntity> findAll(Specification<TEntity> spec, PaginationRequest pagination, boolean withDeleted) {
        pagination.validate();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<TEntity> dataQuery = cb.createQuery(entityClass);
        Root<TEntity> dataRoot = dataQuery.from(entityClass);

        List<Predicate> predicates = new ArrayList<>();
        if (!withDeleted) {
            predicates.add(dataRoot.get("deletedDate").isNull());
        }
        if (spec != null) {
            predicates.add(spec.toPredicate(dataRoot, dataQuery, cb));
        }

        if (pagination.getCursor() != null && !pagination.getCursor().isBlank()) {
            String decoded = new String(Base64.getDecoder().decode(pagination.getCursor()));
            String[] parts = decoded.split("::");
            if (parts.length == 2) {
                LocalDateTime lastDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(parts[0])), ZoneOffset.UTC);
                String lastId = parts[1];

                Predicate dateLess = cb.lessThan(dataRoot.get("createdDate"), lastDate);
                Predicate dateEq = cb.equal(dataRoot.get("createdDate"), lastDate);
                Predicate idLess = cb.lessThan(dataRoot.get("id").as(String.class), lastId);
                Predicate dateEqAndIdLess = cb.and(dateEq, idLess);
                predicates.add(cb.or(dateLess, dateEqAndIdLess));
            }

            dataQuery.where(cb.and(predicates.toArray(Predicate[]::new)));
            dataQuery.orderBy(cb.desc(dataRoot.get("createdDate")), cb.desc(dataRoot.get("id").as(String.class)));

            List<TEntity> items = entityManager.createQuery(dataQuery)
                    .setMaxResults(pagination.getPageSize())
                    .getResultList();

            String nextCursor = null;
            if (!items.isEmpty()) {
                TEntity lastItem = items.get(items.size() - 1);
                long epoch = lastItem.getCreatedDate().toInstant(ZoneOffset.UTC).toEpochMilli();
                nextCursor = Base64.getEncoder().encodeToString((epoch + "::" + lastItem.getId().toString()).getBytes());
            }

            return new Paginate<>(items, nextCursor, pagination.getPageSize());

        } else {
            dataQuery.where(cb.and(predicates.toArray(Predicate[]::new)));
            dataQuery.orderBy(cb.desc(dataRoot.get("createdDate")));

            List<TEntity> items = entityManager.createQuery(dataQuery)
                    .setFirstResult(pagination.getPageIndex() * pagination.getPageSize())
                    .setMaxResults(pagination.getPageSize())
                    .getResultList();

            CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
            Root<TEntity> countRoot = countQuery.from(entityClass);
            countQuery.select(cb.count(countRoot))
                      .where(buildPredicate(spec, countRoot, countQuery, cb, withDeleted));

            long totalCount = entityManager.createQuery(countQuery).getSingleResult();

            return new Paginate<>(items, pagination.getPageIndex(), pagination.getPageSize(), totalCount);
        }
    }

    private void extractAndSaveDomainEvents(TEntity entity) {
        try {
            Method pullMethod = entity.getClass().getMethod("pullDomainEvents");
            @SuppressWarnings("unchecked")
            List<tr.com.huseyinaydin.sharedkernel.events.DomainEvent> events = 
                (List<tr.com.huseyinaydin.sharedkernel.events.DomainEvent>) pullMethod.invoke(entity);
            
            if (events != null && !events.isEmpty()) {
                for (tr.com.huseyinaydin.sharedkernel.events.DomainEvent event : events) {
                    tr.com.huseyinaydin.domain.outbox.DomainEventOutbox outbox = 
                        new tr.com.huseyinaydin.domain.outbox.DomainEventOutbox(
                            event.getEventId(),
                            event.getAggregateType(),
                            event.getAggregateId(),
                            event.getClass().getName(),
                            objectMapper.writeValueAsString(event),
                            event.getOccurredAt()
                    );
                    entityManager.persist(outbox);
                }
            }
        } catch (NoSuchMethodException e) {
            // No domain events support on this entity
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(JpaRepositoryBase.class)
                .error("Failed to extract and save domain events for entity: {}", entity.getClass().getSimpleName(), e);
        }
    }

    @Override
    @io.github.resilience4j.retry.annotation.Retry(name = "jpaRetry")
    public TEntity save(TEntity entity) {
        entityManager.persist(entity);
        extractAndSaveDomainEvents(entity);
        return entity;
    }

    @Override
    public List<TEntity> saveAll(List<TEntity> entities) {
        entities.forEach(this::save);
        return entities;
    }

    @Override
    @io.github.resilience4j.retry.annotation.Retry(name = "jpaRetry")
    public TEntity update(TEntity entity) {
        entity.markAsUpdated();
        TEntity merged = entityManager.merge(entity);
        extractAndSaveDomainEvents(entity);
        return merged;
    }

    @Override
    public void delete(TEntity entity, boolean permanent) {
        if (permanent) {
            TEntity managed = entityManager.contains(entity) ? entity : entityManager.merge(entity);
            entityManager.remove(managed);
            extractAndSaveDomainEvents(entity);
        } else {
            entity.markAsDeleted();
            TEntity merged = entityManager.merge(entity);
            extractAndSaveDomainEvents(entity);
        }
    }

    @Override
    public boolean existsBy(Specification<TEntity> spec) {
        return count(spec) > 0;
    }

    @Override
    public long count(Specification<TEntity> spec) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<TEntity> root = query.from(entityClass);
        query.select(cb.count(root))
             .where(buildPredicate(spec, root, query, cb, false));
        return entityManager.createQuery(query).getSingleResult();
    }

    private Predicate buildPredicate(Specification<TEntity> spec,
                                      Root<TEntity> root,
                                      CriteriaQuery<?> query,
                                      CriteriaBuilder cb,
                                      boolean withDeleted) {
        List<Predicate> predicates = new ArrayList<>();
        if (!withDeleted) {
            predicates.add(root.get("deletedDate").isNull());
        }
        if (spec != null) {
            predicates.add(spec.toPredicate(root, query, cb));
        }
        return predicates.isEmpty()
                ? cb.conjunction()
                : cb.and(predicates.toArray(Predicate[]::new));
    }
}
