package tr.com.huseyinaydin.application.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import tr.com.huseyinaydin.application.customers.dtos.CorporateCustomerResponse;
import tr.com.huseyinaydin.domain.customer.CorporateCustomer;
import tr.com.huseyinaydin.sharedkernel.pagination.Paginate;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(config = MapStructConfig.class)
public interface CorporateCustomerMapper {

    @Mapping(target = "isActive", source = "active")
    CorporateCustomerResponse toResponse(CorporateCustomer entity);

    default Paginate<CorporateCustomerResponse> toResponsePage(Paginate<CorporateCustomer> page) {
        List<CorporateCustomerResponse> items = page.getItems().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return new Paginate<>(items, page.getPageIndex(), page.getPageSize(), page.getTotalCount());
    }
}
