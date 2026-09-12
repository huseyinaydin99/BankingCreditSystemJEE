package tr.com.huseyinaydin.application.stubs;

import tr.com.huseyinaydin.domain.customer.CorporateCustomer;
import tr.com.huseyinaydin.domain.repositories.ICorporateCustomerRepository;
import java.util.Optional;
import java.util.UUID;

public class InMemoryCorporateCustomerRepository extends InMemoryRepositoryBase<CorporateCustomer, UUID> implements ICorporateCustomerRepository {

    @Override
    public Optional<CorporateCustomer> findByTaxNumber(String taxNumber) {
        return store.values().stream()
                .filter(c -> taxNumber.equals(c.getTaxNumber()))
                .findFirst();
    }

    @Override
    public Optional<CorporateCustomer> findByTradeRegistrationNumber(String tradeRegistrationNumber) {
        return store.values().stream()
                .filter(c -> tradeRegistrationNumber.equals(c.getTradeRegistrationNumber()))
                .findFirst();
    }
}
