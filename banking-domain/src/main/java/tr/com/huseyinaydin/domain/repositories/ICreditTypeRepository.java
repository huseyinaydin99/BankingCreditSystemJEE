package tr.com.huseyinaydin.domain.repositories;

import tr.com.huseyinaydin.domain.credittype.CreditType;
import tr.com.huseyinaydin.domain.enums.CustomerType;
import tr.com.huseyinaydin.sharedkernel.pagination.Paginate;
import tr.com.huseyinaydin.sharedkernel.pagination.PaginationRequest;

import java.util.List;
import java.util.UUID;

public interface ICreditTypeRepository extends IAsyncRepository<CreditType, UUID> {

    Paginate<CreditType> findByCustomerType(CustomerType type, PaginationRequest pagination);

    List<CreditType> findSubTypes(UUID parentId);
}
