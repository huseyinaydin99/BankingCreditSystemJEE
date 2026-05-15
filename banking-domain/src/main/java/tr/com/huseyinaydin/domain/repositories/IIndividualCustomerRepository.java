package tr.com.huseyinaydin.domain.repositories;

import tr.com.huseyinaydin.domain.customer.IndividualCustomer;

import java.util.Optional;

public interface IIndividualCustomerRepository extends ICustomerRepository<IndividualCustomer> {

    Optional<IndividualCustomer> findByNationalId(String nationalId);

    Optional<IndividualCustomer> findByEmail(String email);
}
