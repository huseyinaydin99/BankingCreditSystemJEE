package tr.com.huseyinaydin.application.ports.read;

import tr.com.huseyinaydin.application.creditapplication.dtos.CreditApplicationResponse;
import tr.com.huseyinaydin.sharedkernel.pagination.Paginate;

import java.util.UUID;

public interface ICreditApplicationReadService {
    CreditApplicationResponse getById(UUID id);
    Paginate<CreditApplicationResponse> getListByCustomerId(UUID customerId, int pageIndex, int pageSize);
    java.util.List<tr.com.huseyinaydin.application.creditapplication.dtos.PendingApplicationReminderDto> getPendingApplicationsOlderThan(java.time.Instant olderThan);
}
