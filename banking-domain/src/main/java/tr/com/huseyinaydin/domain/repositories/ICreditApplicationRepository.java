package tr.com.huseyinaydin.domain.repositories;

import tr.com.huseyinaydin.domain.creditapplication.CreditApplication;
import tr.com.huseyinaydin.domain.enums.CreditApplicationStatus;
import tr.com.huseyinaydin.sharedkernel.pagination.Paginate;
import tr.com.huseyinaydin.sharedkernel.pagination.PaginationRequest;

import java.util.UUID;

public interface ICreditApplicationRepository extends IAsyncRepository<CreditApplication, UUID> {

    Paginate<CreditApplication> findByCustomerId(UUID customerId, PaginationRequest pagination);

    Paginate<CreditApplication> findByStatus(CreditApplicationStatus status, PaginationRequest pagination);
}
