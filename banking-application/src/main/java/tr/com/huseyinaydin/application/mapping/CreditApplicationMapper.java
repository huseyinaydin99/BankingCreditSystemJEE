package tr.com.huseyinaydin.application.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import tr.com.huseyinaydin.application.creditapplication.dtos.CreditApplicationResponse;
import tr.com.huseyinaydin.domain.creditapplication.CreditApplication;
import tr.com.huseyinaydin.domain.credittype.CreditType;

@Mapper(config = MapStructConfig.class)
public interface CreditApplicationMapper {

    @Mapping(target = "id", source = "app.id")
    @Mapping(target = "createdDate", source = "app.createdDate")
    @Mapping(target = "customerId",
             expression = "java(app.getCustomerId())")
    @Mapping(target = "customerName",
             expression = "java(app.getCustomer() != null ? app.getCustomer().getFullName() : \"\")")
    @Mapping(target = "creditTypeId",
             source = "app.creditTypeId")
    @Mapping(target = "creditTypeName",
             expression = "java(creditType != null ? creditType.getName() : \"\")")
    @Mapping(target = "approvedAmount",
             expression = "java(app.getApprovedAmount() != null ? app.getApprovedAmount().getAmount() : null)")
    @Mapping(target = "monthlyPayment",
             expression = "java(app.getMonthlyPayment() != null ? app.getMonthlyPayment().getAmount() : null)")
    @Mapping(target = "totalPayment",
             expression = "java(app.getTotalPayment() != null ? app.getTotalPayment().getAmount() : null)")
    CreditApplicationResponse toResponse(CreditApplication app, CreditType creditType);
}
