package tr.com.huseyinaydin.application.ports.read;

import tr.com.huseyinaydin.application.creditapplication.dtos.CreditApplicationResponse;
import tr.com.huseyinaydin.sharedkernel.pagination.Paginate;

import java.util.UUID;

public interface ICreditApplicationReadService {
    Paginate<CreditApplicationResponse> getListByCustomerId(UUID customerId, int pageIndex, int pageSize);
}
