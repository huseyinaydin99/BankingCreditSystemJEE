package tr.com.huseyinaydin.application.stubs;

import tr.com.huseyinaydin.domain.customer.IndividualCustomer;
import tr.com.huseyinaydin.domain.repositories.IIndividualCustomerRepository;
import java.util.Optional;
import java.util.UUID;

public class InMemoryIndividualCustomerRepository extends InMemoryRepositoryBase<IndividualCustomer, UUID> implements IIndividualCustomerRepository {

    @Override
    public Optional<IndividualCustomer> findByNationalId(String nationalId) {
        return store.values().stream()
                .filter(c -> nationalId.equals(c.getNationalId()))
                .findFirst();
    }

    @Override
    public Optional<IndividualCustomer> findByEmail(String email) {
        return store.values().stream()
                .filter(c -> email.equals(c.getEmail()))
                .findFirst();
    }
}
