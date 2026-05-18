package tr.com.huseyinaydin.application.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ObjectFactory;
import tr.com.huseyinaydin.application.customers.commands.CreateCorporateCustomerCommand;
import tr.com.huseyinaydin.application.customers.dtos.CorporateCustomerResponse;
import tr.com.huseyinaydin.domain.customer.CorporateCustomer;
import tr.com.huseyinaydin.sharedkernel.pagination.Paginate;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(config = MapStructConfig.class)
public interface CorporateCustomerMapper {

    @Mapping(target = "isActive", source = "active")
    CorporateCustomerResponse toResponse(CorporateCustomer entity);

    @Mapping(target = "active", ignore = true)
    CorporateCustomer toEntity(CreateCorporateCustomerCommand command);

    @ObjectFactory
    default CorporateCustomer createCorporateCustomer(CreateCorporateCustomerCommand command) {
        return new CorporateCustomer(
                command.companyName(),
                command.taxNumber(),
                command.email()
        );
    }

    default Paginate<CorporateCustomerResponse> toResponsePage(Paginate<CorporateCustomer> page) {
        List<CorporateCustomerResponse> items = page.getItems().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return new Paginate<>(items, page.getPageIndex(), page.getPageSize(), page.getTotalCount());
    }
}
