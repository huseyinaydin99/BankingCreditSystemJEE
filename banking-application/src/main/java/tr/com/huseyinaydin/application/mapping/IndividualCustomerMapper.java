package tr.com.huseyinaydin.application.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ObjectFactory;
import tr.com.huseyinaydin.application.customers.commands.CreateIndividualCustomerCommand;
import tr.com.huseyinaydin.application.customers.dtos.IndividualCustomerResponse;
import tr.com.huseyinaydin.domain.customer.IndividualCustomer;
import tr.com.huseyinaydin.sharedkernel.pagination.Paginate;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(config = MapStructConfig.class)
public interface IndividualCustomerMapper {

    // entity.isActive() getter maps to boolean property "active";
    // record parameter is "isActive" — explicit mapping needed
    @Mapping(target = "isActive", source = "active")
    IndividualCustomerResponse toResponse(IndividualCustomer entity);

    // Uses @ObjectFactory below — MapStruct calls public constructor,
    // then sets remaining fields via setters
    @Mapping(target = "active", ignore = true)
    IndividualCustomer toEntity(CreateIndividualCustomerCommand command);

    @ObjectFactory
    default IndividualCustomer createIndividualCustomer(CreateIndividualCustomerCommand command) {
        return new IndividualCustomer(
                command.firstName(),
                command.lastName(),
                command.nationalId(),
                command.email()
        );
    }

    default Paginate<IndividualCustomerResponse> toResponsePage(Paginate<IndividualCustomer> page) {
        List<IndividualCustomerResponse> items = page.getItems().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return new Paginate<>(items, page.getPageIndex(), page.getPageSize(), page.getTotalCount());
    }
}
