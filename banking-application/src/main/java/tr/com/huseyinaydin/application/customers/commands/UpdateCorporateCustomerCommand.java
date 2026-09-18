package tr.com.huseyinaydin.application.customers.commands;

import jakarta.validation.constraints.NotBlank;
import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.customers.dtos.UpdatedCorporateCustomerResponse;
import tr.com.huseyinaydin.application.customers.rules.CorporateCustomerBusinessRules;
import tr.com.huseyinaydin.application.ports.IMapper;
import tr.com.huseyinaydin.domain.repositories.ICorporateCustomerRepository;
import tr.com.huseyinaydin.application.validation.constraints.TradeRegistrationNumber;
import tr.com.huseyinaydin.domain.customer.CorporateCustomer;
import tr.com.huseyinaydin.sharedkernel.messaging.ICommand;
import tr.com.huseyinaydin.sharedkernel.messaging.ICommandHandler;

import java.time.LocalDate;
import java.util.UUID;

public record UpdateCorporateCustomerCommand(
        UUID id,
        String companyName,
        @NotBlank @TradeRegistrationNumber String tradeRegistrationNumber,
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

        private final ICorporateCustomerRepository corporateCustomers;
        private final CorporateCustomerBusinessRules businessRules;
        private final IMapper mapper;

        public Handler(ICorporateCustomerRepository corporateCustomers,
                       CorporateCustomerBusinessRules businessRules,
                       IMapper mapper) {
            this.corporateCustomers = corporateCustomers;
            this.businessRules = businessRules;
            this.mapper = mapper;
        }

        @Override
        public UpdatedCorporateCustomerResponse handle(UpdateCorporateCustomerCommand command) {
            businessRules.customerShouldExistWhenRequested(command.id());

            CorporateCustomer customer = corporateCustomers
                    .findById(command.id())
                    .orElseThrow();

            businessRules.customerShouldBeActive(customer);
            businessRules.tradeRegistrationNumberMustBeUniqueForUpdate(
                    command.tradeRegistrationNumber(), command.id());

            
            
            
            
            
            customer.updateCompanyInfo(command.companyName(), command.taxOffice(), command.companyRegistrationNumber(), command.tradeRegistrationNumber(), command.authorizedPersonName(), command.companyFoundationDate());
            
            
            customer.updateContactInfo(command.phoneNumber(), command.email(), command.address());

            corporateCustomers.update(customer);

            return new UpdatedCorporateCustomerResponse(
                    customer.getId(),
                    customer.getCompanyName(),
                    customer.getEmail(),
                    "Kurumsal müşteri başarıyla güncellendi"
            );
        }
    }
}
