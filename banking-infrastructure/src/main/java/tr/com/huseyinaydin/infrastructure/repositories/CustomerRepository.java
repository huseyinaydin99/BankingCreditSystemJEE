package tr.com.huseyinaydin.infrastructure.repositories;

import jakarta.persistence.EntityManager;
import tr.com.huseyinaydin.domain.customer.Customer;
import tr.com.huseyinaydin.domain.repositories.ICustomerRepository;

import java.util.UUID;

public abstract class CustomerRepository<TCustomer extends Customer>
        extends JpaRepositoryBase<TCustomer, UUID>
        implements ICustomerRepository<TCustomer> {

    protected CustomerRepository(EntityManager entityManager, Class<TCustomer> customerClass) {
        super(entityManager, customerClass);
    }
}
