package tr.com.huseyinaydin.infrastructure.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import tr.com.huseyinaydin.domain.customer.IndividualCustomer;
import tr.com.huseyinaydin.domain.repositories.IIndividualCustomerRepository;

import java.util.Optional;

public class IndividualCustomerJpaRepository
        extends CustomerRepository<IndividualCustomer>
        implements IIndividualCustomerRepository {

    public IndividualCustomerJpaRepository(EntityManager entityManager) {
        super(entityManager, IndividualCustomer.class);
    }

    @Override
    public Optional<IndividualCustomer> findByNationalId(String nationalId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<IndividualCustomer> query = cb.createQuery(IndividualCustomer.class);
        Root<IndividualCustomer> root = query.from(IndividualCustomer.class);
        query.where(cb.and(
                cb.equal(root.get("nationalId"), nationalId),
                root.get("deletedDate").isNull()
        ));
        return entityManager.createQuery(query).getResultStream().findFirst();
    }

    @Override
    public Optional<IndividualCustomer> findByEmail(String email) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<IndividualCustomer> query = cb.createQuery(IndividualCustomer.class);
        Root<IndividualCustomer> root = query.from(IndividualCustomer.class);
        query.where(cb.and(
                cb.equal(root.get("email"), email),
                root.get("deletedDate").isNull()
        ));
        return entityManager.createQuery(query).getResultStream().findFirst();
    }
}
