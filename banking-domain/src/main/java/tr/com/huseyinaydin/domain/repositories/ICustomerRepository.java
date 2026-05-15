package tr.com.huseyinaydin.domain.repositories;

import tr.com.huseyinaydin.domain.customer.Customer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ICustomerRepository<TCustomer extends Customer> {
    Optional<TCustomer> findById(UUID id);
    TCustomer save(TCustomer customer);
    void delete(UUID id);
    List<TCustomer> findAll();
    boolean existsById(UUID id);
}
