package tr.com.huseyinaydin.application.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import tr.com.huseyinaydin.application.customers.dtos.IndividualCustomerResponse;
import tr.com.huseyinaydin.domain.customer.IndividualCustomer;
import tr.com.huseyinaydin.sharedkernel.pagination.Paginate;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(config = MapStructConfig.class)
public interface IndividualCustomerMapper {

    @Mapping(target = "isActive", source = "active")
    IndividualCustomerResponse toResponse(IndividualCustomer entity);

    default Paginate<IndividualCustomerResponse> toResponsePage(Paginate<IndividualCustomer> page) {
        List<IndividualCustomerResponse> items = page.getItems().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return new Paginate<>(items, page.getPageIndex(), page.getPageSize(), page.getTotalCount());
    }
}
