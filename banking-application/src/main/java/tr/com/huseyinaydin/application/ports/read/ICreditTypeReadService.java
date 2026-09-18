package tr.com.huseyinaydin.application.ports.read;

import tr.com.huseyinaydin.application.credittype.dtos.CreditTypeResponse;
import tr.com.huseyinaydin.domain.enums.CustomerType;
import tr.com.huseyinaydin.sharedkernel.pagination.Paginate;

import java.util.UUID;

public interface ICreditTypeReadService {
    Paginate<CreditTypeResponse> getList(CustomerType customerType, UUID parentCreditTypeId, int pageIndex, int pageSize);
}
