package tr.com.huseyinaydin.infrastructure.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import tr.com.huseyinaydin.domain.credittype.CreditType;
import tr.com.huseyinaydin.domain.enums.CustomerType;
import tr.com.huseyinaydin.domain.repositories.ICreditTypeRepository;
import tr.com.huseyinaydin.sharedkernel.pagination.Paginate;
import tr.com.huseyinaydin.sharedkernel.pagination.PaginationRequest;

import java.util.List;
import java.util.UUID;

public class CreditTypeJpaRepository
        extends JpaRepositoryBase<CreditType, UUID>
        implements ICreditTypeRepository {

    public CreditTypeJpaRepository(EntityManager entityManager) {
        super(entityManager, CreditType.class);
    }

    @Override
    public Paginate<CreditType> findByCustomerType(CustomerType type, PaginationRequest pagination) {
        pagination.validate();
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<CreditType> dataQuery = cb.createQuery(CreditType.class);
        Root<CreditType> dataRoot = dataQuery.from(CreditType.class);
        Predicate dataPredicate = cb.and(
                cb.equal(dataRoot.get("customerType"), type),
                dataRoot.get("deletedDate").isNull()
        );
        dataQuery.where(dataPredicate);

        List<CreditType> items = entityManager.createQuery(dataQuery)
                .setFirstResult(pagination.getPageIndex() * pagination.getPageSize())
                .setMaxResults(pagination.getPageSize())
                .getResultList();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<CreditType> countRoot = countQuery.from(CreditType.class);
        countQuery.select(cb.count(countRoot)).where(cb.and(
                cb.equal(countRoot.get("customerType"), type),
                countRoot.get("deletedDate").isNull()
        ));
        long total = entityManager.createQuery(countQuery).getSingleResult();

        return new Paginate<>(items, pagination.getPageIndex(), pagination.getPageSize(), total);
    }

    @Override
    public List<CreditType> findSubTypes(UUID parentId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<CreditType> query = cb.createQuery(CreditType.class);
        Root<CreditType> root = query.from(CreditType.class);
        query.where(cb.and(
                cb.equal(root.get("parentCreditTypeId"), parentId),
                root.get("deletedDate").isNull()
        ));
        return entityManager.createQuery(query).getResultList();
    }
}
