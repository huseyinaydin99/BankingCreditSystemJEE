package tr.com.huseyinaydin.application.customers.commands;

import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.customers.dtos.UpdatedCorporateCustomerResponse;
import tr.com.huseyinaydin.application.customers.rules.CorporateCustomerBusinessRules;
import tr.com.huseyinaydin.application.ports.IMapper;
import tr.com.huseyinaydin.application.ports.IUnitOfWork;
import tr.com.huseyinaydin.domain.customer.CorporateCustomer;
import tr.com.huseyinaydin.sharedkernel.messaging.ICommand;
import tr.com.huseyinaydin.sharedkernel.messaging.ICommandHandler;

import java.time.LocalDate;
import java.util.UUID;

public record UpdateCorporateCustomerCommand(
        UUID id,
        String companyName,
        String taxOffice,
        String companyRegistrationNumber,
        String authorizedPersonName,
        LocalDate companyFoundationDate,
        String phoneNumber,
        String email,
        String address
) implements ICommand<UpdatedCorporateCustomerResponse> {

    @Component
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public static class Handler
            implements ICommandHandler<UpdateCorporateCustomerCommand, UpdatedCorporateCustomerResponse> {

        private final IUnitOfWork uow;
        private final CorporateCustomerBusinessRules businessRules;
        private final IMapper mapper;

        public Handler(IUnitOfWork uow,
                       CorporateCustomerBusinessRules businessRules,
                       IMapper mapper) {
            this.uow = uow;
            this.businessRules = businessRules;
            this.mapper = mapper;
        }

        @Override
        public UpdatedCorporateCustomerResponse handle(UpdateCorporateCustomerCommand command) {
            businessRules.customerShouldExistWhenRequested(command.id());

            CorporateCustomer customer = uow.corporateCustomers()
                    .findById(command.id())
                    .orElseThrow();

            businessRules.customerShouldBeActive(customer);

            customer.setCompanyName(command.companyName());
            customer.setTaxOffice(command.taxOffice());
            customer.setCompanyRegistrationNumber(command.companyRegistrationNumber());
            customer.setAuthorizedPersonName(command.authorizedPersonName());
            customer.setCompanyFoundationDate(command.companyFoundationDate());
            customer.setPhoneNumber(command.phoneNumber());
            customer.setEmail(command.email());
            customer.setAddress(command.address());

            uow.beginTransaction();
            uow.corporateCustomers().update(customer);
            uow.commit();

            return new UpdatedCorporateCustomerResponse(
                    customer.getId(),
                    customer.getCompanyName(),
                    customer.getEmail(),
                    "Kurumsal müşteri başarıyla güncellendi"
            );
        }
    }
}
