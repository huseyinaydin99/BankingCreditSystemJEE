package tr.com.huseyinaydin.infrastructure.repositories;

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

import java.util.List;
import java.util.UUID;

public class CreditApplicationJpaRepository
        extends JpaRepositoryBase<CreditApplication, UUID>
        implements ICreditApplicationRepository {

    public CreditApplicationJpaRepository(EntityManager entityManager) {
        super(entityManager, CreditApplication.class);
    }

    @Override
    public Paginate<CreditApplication> findByCustomerId(UUID customerId, PaginationRequest pagination) {
        pagination.validate();
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<CreditApplication> dataQuery = cb.createQuery(CreditApplication.class);
        Root<CreditApplication> dataRoot = dataQuery.from(CreditApplication.class);
        Predicate dataPredicate = cb.and(
                cb.equal(dataRoot.get("customerId"), customerId),
                dataRoot.get("deletedDate").isNull()
        );
        dataQuery.where(dataPredicate);

        List<CreditApplication> items = entityManager.createQuery(dataQuery)
                .setFirstResult(pagination.getPageIndex() * pagination.getPageSize())
                .setMaxResults(pagination.getPageSize())
                .getResultList();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<CreditApplication> countRoot = countQuery.from(CreditApplication.class);
        countQuery.select(cb.count(countRoot)).where(cb.and(
                cb.equal(countRoot.get("customerId"), customerId),
                countRoot.get("deletedDate").isNull()
        ));
        long total = entityManager.createQuery(countQuery).getSingleResult();

        return new Paginate<>(items, pagination.getPageIndex(), pagination.getPageSize(), total);
    }

    @Override
    public Paginate<CreditApplication> findByStatus(CreditApplicationStatus status, PaginationRequest pagination) {
        pagination.validate();
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<CreditApplication> dataQuery = cb.createQuery(CreditApplication.class);
        Root<CreditApplication> dataRoot = dataQuery.from(CreditApplication.class);
        Predicate dataPredicate = cb.and(
                cb.equal(dataRoot.get("status"), status),
                dataRoot.get("deletedDate").isNull()
        );
        dataQuery.where(dataPredicate);

        List<CreditApplication> items = entityManager.createQuery(dataQuery)
                .setFirstResult(pagination.getPageIndex() * pagination.getPageSize())
                .setMaxResults(pagination.getPageSize())
                .getResultList();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<CreditApplication> countRoot = countQuery.from(CreditApplication.class);
        countQuery.select(cb.count(countRoot)).where(cb.and(
                cb.equal(countRoot.get("status"), status),
                countRoot.get("deletedDate").isNull()
        ));
        long total = entityManager.createQuery(countQuery).getSingleResult();

        return new Paginate<>(items, pagination.getPageIndex(), pagination.getPageSize(), total);
    }
}
