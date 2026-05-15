package tr.com.huseyinaydin.domain.repositories;

import tr.com.huseyinaydin.domain.customer.CorporateCustomer;

import java.util.Optional;

public interface ICorporateCustomerRepository extends ICustomerRepository<CorporateCustomer> {

    Optional<CorporateCustomer> findByTaxNumber(String taxNumber);
}
