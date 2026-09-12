package tr.com.huseyinaydin.application.stubs;

import tr.com.huseyinaydin.domain.credittype.CreditType;
import tr.com.huseyinaydin.domain.enums.CustomerType;
import tr.com.huseyinaydin.domain.repositories.ICreditTypeRepository;
import tr.com.huseyinaydin.sharedkernel.pagination.Paginate;
import tr.com.huseyinaydin.sharedkernel.pagination.PaginationRequest;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class InMemoryCreditTypeRepository extends InMemoryRepositoryBase<CreditType, UUID> implements ICreditTypeRepository {

    @Override
    public Paginate<CreditType> findByCustomerType(CustomerType type, PaginationRequest pagination) {
        return new Paginate<>(new ArrayList<>(), 0, 10, 0);
    }

    @Override
    public List<CreditType> findSubTypes(UUID parentId) {
        return store.values().stream()
                .filter(c -> parentId.equals(c.getParentCreditTypeId()))
                .collect(Collectors.toList());
    }
}
