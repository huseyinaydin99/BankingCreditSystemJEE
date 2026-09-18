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

public abstract class JpaRepositoryBase<TEntity extends BaseEntity<TId>, TId>
        implements IAsyncRepository<TEntity, TId> {

    protected final EntityManager entityManager;
    protected final Class<TEntity> entityClass;

    protected JpaRepositoryBase(EntityManager entityManager, Class<TEntity> entityClass) {
        this.entityManager = entityManager;
        this.entityClass = entityClass;
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
            // Keyset Pagination (Cursor tabanlı)
            String decoded = new String(Base64.getDecoder().decode(pagination.getCursor()));
            String[] parts = decoded.split("::");
            if (parts.length == 2) {
                LocalDateTime lastDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(parts[0])), ZoneOffset.UTC);
                String lastId = parts[1];

                // (createdDate < lastDate) OR (createdDate = lastDate AND id < lastId)
                Predicate dateLess = cb.lessThan(dataRoot.get("createdDate"), lastDate);
                Predicate dateEq = cb.equal(dataRoot.get("createdDate"), lastDate);
                // id cannot be universally compared generically easily in criteria if it's UUID.
                // For simplicity, we just use dateLess if id comparison is complex. 
                // Or we do string conversion.
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
            // Traditional Offset Pagination (Geriye Dönük Uyumluluk İçin)
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

    @Override
    @io.github.resilience4j.retry.annotation.Retry(name = "jpaRetry")
    public TEntity save(TEntity entity) {
        entityManager.persist(entity);
        return entity;
    }

    @Override
    public List<TEntity> saveAll(List<TEntity> entities) {
        entities.forEach(entityManager::persist);
        return entities;
    }

    @Override
    @io.github.resilience4j.retry.annotation.Retry(name = "jpaRetry")
    public TEntity update(TEntity entity) {
        entity.markAsUpdated();
        return entityManager.merge(entity);
    }

    @Override
    public void delete(TEntity entity, boolean permanent) {
        if (permanent) {
            TEntity managed = entityManager.contains(entity) ? entity : entityManager.merge(entity);
            entityManager.remove(managed);
        } else {
            entity.markAsDeleted();
            entityManager.merge(entity);
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
