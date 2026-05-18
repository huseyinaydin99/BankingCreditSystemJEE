package tr.com.huseyinaydin.application.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import tr.com.huseyinaydin.application.creditapplication.dtos.CreditApplicationResponse;
import tr.com.huseyinaydin.domain.creditapplication.CreditApplication;
import tr.com.huseyinaydin.domain.credittype.CreditType;

@Mapper(config = MapStructConfig.class)
public interface CreditApplicationMapper {

    // app.getCustomerId() — convenience method on entity
    // app.getCustomer().getFullName() — requires lazy-loaded Customer; safe within a transaction
    // creditType — second source parameter; may be null if credit type is deleted
    @Mapping(target = "customerId",
             expression = "java(app.getCustomerId())")
    @Mapping(target = "customerName",
             expression = "java(app.getCustomer() != null ? app.getCustomer().getFullName() : \"\")")
    @Mapping(target = "creditTypeId",
             source = "app.creditTypeId")
    @Mapping(target = "creditTypeName",
             expression = "java(creditType != null ? creditType.getName() : \"\")")
    CreditApplicationResponse toResponse(CreditApplication app, CreditType creditType);
}
