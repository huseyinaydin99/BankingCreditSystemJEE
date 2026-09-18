package tr.com.huseyinaydin.persistence.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import tr.com.huseyinaydin.domain.creditapplication.CreditApplication;
import tr.com.huseyinaydin.domain.enums.CreditApplicationStatus;
import tr.com.huseyinaydin.domain.repositories.ICreditApplicationRepository;
import tr.com.huseyinaydin.sharedkernel.pagination.Paginate;
import tr.com.huseyinaydin.sharedkernel.pagination.PaginationRequest;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;

public class CreditApplicationJpaRepository
        extends JpaRepositoryBase<CreditApplication, UUID>
        implements ICreditApplicationRepository {

    public CreditApplicationJpaRepository(EntityManager entityManager) {
        super(entityManager, CreditApplication.class);
    }

    @Override
    public Paginate<CreditApplication> findByCustomerId(UUID customerId, PaginationRequest pagination) {
        return executeQuery(pagination, (cb, root) -> cb.equal(root.get("customerId"), customerId));
    }

    @Override
    public Paginate<CreditApplication> findByStatus(CreditApplicationStatus status, PaginationRequest pagination) {
        return executeQuery(pagination, (cb, root) -> cb.equal(root.get("status"), status));
    }

    private Paginate<CreditApplication> executeQuery(PaginationRequest pagination, BiFunction<CriteriaBuilder, Root<CreditApplication>, Predicate> spec) {
        pagination.validate();
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<CreditApplication> dataQuery = cb.createQuery(CreditApplication.class);
        Root<CreditApplication> dataRoot = dataQuery.from(CreditApplication.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(spec.apply(cb, dataRoot));
        predicates.add(dataRoot.get("deletedDate").isNull());

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

            List<CreditApplication> items = entityManager.createQuery(dataQuery)
                    .setMaxResults(pagination.getPageSize())
                    .getResultList();

            String nextCursor = null;
            if (!items.isEmpty()) {
                CreditApplication lastItem = items.get(items.size() - 1);
                long epoch = lastItem.getCreatedDate().toInstant(ZoneOffset.UTC).toEpochMilli();
                nextCursor = Base64.getEncoder().encodeToString((epoch + "::" + lastItem.getId().toString()).getBytes());
            }

            return new Paginate<>(items, nextCursor, pagination.getPageSize());

        } else {
            dataQuery.where(cb.and(predicates.toArray(Predicate[]::new)));
            dataQuery.orderBy(cb.desc(dataRoot.get("createdDate")));

            List<CreditApplication> items = entityManager.createQuery(dataQuery)
                    .setFirstResult(pagination.getPageIndex() * pagination.getPageSize())
                    .setMaxResults(pagination.getPageSize())
                    .getResultList();

            CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
            Root<CreditApplication> countRoot = countQuery.from(CreditApplication.class);
            
            countQuery.select(cb.count(countRoot)).where(cb.and(
                    spec.apply(cb, countRoot),
                    countRoot.get("deletedDate").isNull()
            ));
            long total = entityManager.createQuery(countQuery).getSingleResult();

            return new Paginate<>(items, pagination.getPageIndex(), pagination.getPageSize(), total);
        }
    }
}
