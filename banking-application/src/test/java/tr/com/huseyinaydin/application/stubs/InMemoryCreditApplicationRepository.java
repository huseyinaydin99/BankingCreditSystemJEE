package tr.com.huseyinaydin.application.stubs;

import tr.com.huseyinaydin.domain.creditapplication.CreditApplication;
import tr.com.huseyinaydin.domain.enums.CreditApplicationStatus;
import tr.com.huseyinaydin.domain.repositories.ICreditApplicationRepository;
import tr.com.huseyinaydin.sharedkernel.pagination.Paginate;
import tr.com.huseyinaydin.sharedkernel.pagination.PaginationRequest;
import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collectors;

public class InMemoryCreditApplicationRepository extends InMemoryRepositoryBase<CreditApplication, UUID> implements ICreditApplicationRepository {

    @Override
    public Paginate<CreditApplication> findByCustomerId(UUID customerId, PaginationRequest pagination) {
        return new Paginate<>(new ArrayList<>(), 0, 10, 0);
    }

    @Override
    public Paginate<CreditApplication> findByStatus(CreditApplicationStatus status, PaginationRequest pagination) {
        return new Paginate<>(new ArrayList<>(), 0, 10, 0);
    }
}
