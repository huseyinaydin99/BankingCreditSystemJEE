package tr.com.huseyinaydin.infrastructure.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import tr.com.huseyinaydin.domain.customer.CorporateCustomer;
import tr.com.huseyinaydin.domain.repositories.ICorporateCustomerRepository;

import java.util.Optional;

public class CorporateCustomerJpaRepository
        extends CustomerRepository<CorporateCustomer>
        implements ICorporateCustomerRepository {

    public CorporateCustomerJpaRepository(EntityManager entityManager) {
        super(entityManager, CorporateCustomer.class);
    }

    @Override
    public Optional<CorporateCustomer> findByTaxNumber(String taxNumber) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<CorporateCustomer> query = cb.createQuery(CorporateCustomer.class);
        Root<CorporateCustomer> root = query.from(CorporateCustomer.class);
        query.where(cb.and(
                cb.equal(root.get("taxNumber"), taxNumber),
                root.get("deletedDate").isNull()
        ));
        return entityManager.createQuery(query).getResultStream().findFirst();
    }
}
