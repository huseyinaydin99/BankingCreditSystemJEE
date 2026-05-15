package tr.com.huseyinaydin.domain.repositories;

import tr.com.huseyinaydin.domain.creditapplication.CreditApplication;
import tr.com.huseyinaydin.domain.enums.CreditApplicationStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ICreditApplicationRepository {
    Optional<CreditApplication> findById(UUID id);
    CreditApplication save(CreditApplication application);
    void delete(UUID id);
    List<CreditApplication> findByCustomerId(UUID customerId);
    List<CreditApplication> findByStatus(CreditApplicationStatus status);
    boolean existsById(UUID id);
}
