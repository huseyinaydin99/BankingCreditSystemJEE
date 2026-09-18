package tr.com.huseyinaydin.application.customers.commands;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.customers.dtos.CreatedCorporateCustomerResponse;
import tr.com.huseyinaydin.application.customers.rules.CorporateCustomerBusinessRules;
import tr.com.huseyinaydin.application.ports.IMapper;
import tr.com.huseyinaydin.application.ports.IPasswordHashService;
import tr.com.huseyinaydin.application.ports.PasswordHash;
import tr.com.huseyinaydin.domain.repositories.ICorporateCustomerRepository;
import tr.com.huseyinaydin.application.validation.constraints.PhoneNumber;
import tr.com.huseyinaydin.application.validation.constraints.TurkishTaxNumber;
import tr.com.huseyinaydin.domain.customer.CorporateCustomer;
import tr.com.huseyinaydin.sharedkernel.messaging.ICommand;
import tr.com.huseyinaydin.sharedkernel.messaging.ICommandHandler;

public record CreateCorporateCustomerCommand(
        @NotBlank @Size(max = 100) String companyName,
        @NotBlank @TurkishTaxNumber String taxNumber,
        String tradeRegistrationNumber,
        String taxOffice,
        @NotBlank @PhoneNumber String phoneNumber,
        @NotBlank @Email String email,
        String address,
        @NotBlank String password,
        String authorizedPersonName
) implements ICommand<CreatedCorporateCustomerResponse> {

    @Component
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public static class Handler
            implements ICommandHandler<CreateCorporateCustomerCommand, CreatedCorporateCustomerResponse> {

        private final ICorporateCustomerRepository corporateCustomerRepository;
        private final CorporateCustomerBusinessRules businessRules;
        private final IPasswordHashService passwordHashService;
        private final IMapper mapper;

        public Handler(ICorporateCustomerRepository corporateCustomerRepository,
                       CorporateCustomerBusinessRules businessRules,
                       IPasswordHashService passwordHashService,
                       IMapper mapper) {
            this.corporateCustomerRepository = corporateCustomerRepository;
            this.businessRules = businessRules;
            this.passwordHashService = passwordHashService;
            this.mapper = mapper;
        }

        @Override
        public CreatedCorporateCustomerResponse handle(CreateCorporateCustomerCommand command) {
            businessRules.taxNumberCannotBeDuplicatedWhenInserted(command.taxNumber());

            PasswordHash passwordHash = passwordHashService.createHash(command.password());

            CorporateCustomer customer = new CorporateCustomer(
                    command.companyName(),
                    command.taxNumber(),
                    command.email(),
                    passwordHash.hash(),
                    passwordHash.salt()
            );
            customer.updateCompanyInfo(command.companyName(), command.taxOffice(), null, command.tradeRegistrationNumber(), command.authorizedPersonName(), null);
            customer.updateContactInfo(command.phoneNumber(), command.email(), command.address());

            corporateCustomerRepository.save(customer);

            return new CreatedCorporateCustomerResponse(
                    customer.getId(),
                    customer.getCompanyName(),
                    customer.getTaxNumber(),
                    "Kurumsal müşteri başarıyla oluşturuldu"
            );
        }
    }
}
