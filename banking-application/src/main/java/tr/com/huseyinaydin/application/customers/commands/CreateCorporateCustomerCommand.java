package tr.com.huseyinaydin.application.customers.commands;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.customers.dtos.CreatedCorporateCustomerResponse;
import tr.com.huseyinaydin.application.customers.rules.CorporateCustomerBusinessRules;
import tr.com.huseyinaydin.application.ports.IMapper;
import tr.com.huseyinaydin.application.ports.IPasswordHashService;
import tr.com.huseyinaydin.application.ports.IUnitOfWork;
import tr.com.huseyinaydin.application.validation.constraints.PhoneNumber;
import tr.com.huseyinaydin.application.validation.constraints.TurkishTaxNumber;
import tr.com.huseyinaydin.domain.customer.CorporateCustomer;
import tr.com.huseyinaydin.domain.enums.UserRole;
import tr.com.huseyinaydin.domain.user.ApplicationUser;
import tr.com.huseyinaydin.sharedkernel.messaging.ICommand;
import tr.com.huseyinaydin.sharedkernel.messaging.ICommandHandler;

import java.time.LocalDate;

public record CreateCorporateCustomerCommand(
        @NotBlank @Size(max = 100) String companyName,
        @NotBlank @TurkishTaxNumber String taxNumber,
        String taxOffice,
        String companyRegistrationNumber,
        @NotBlank String authorizedPersonName,
        LocalDate companyFoundationDate,
        @NotBlank @PhoneNumber String phoneNumber,
        @NotBlank @Email String email,
        String address,
        @NotBlank String password
) implements ICommand<CreatedCorporateCustomerResponse> {

    @Component
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public static class Handler
            implements ICommandHandler<CreateCorporateCustomerCommand, CreatedCorporateCustomerResponse> {

        private final IUnitOfWork uow;
        private final CorporateCustomerBusinessRules businessRules;
        private final IPasswordHashService passwordHashService;
        private final IMapper mapper;

        public Handler(IUnitOfWork uow,
                       CorporateCustomerBusinessRules businessRules,
                       IPasswordHashService passwordHashService,
                       IMapper mapper) {
            this.uow = uow;
            this.businessRules = businessRules;
            this.passwordHashService = passwordHashService;
            this.mapper = mapper;
        }

        @Override
        public CreatedCorporateCustomerResponse handle(CreateCorporateCustomerCommand command) {
            businessRules.taxNumberCannotBeDuplicatedWhenInserted(command.taxNumber());

            CorporateCustomer customer = new CorporateCustomer(
                    command.companyName(),
                    command.taxNumber(),
                    command.email()
            );
            customer.setTaxOffice(command.taxOffice());
            customer.setCompanyRegistrationNumber(command.companyRegistrationNumber());
            customer.setAuthorizedPersonName(command.authorizedPersonName());
            customer.setCompanyFoundationDate(command.companyFoundationDate());
            customer.setPhoneNumber(command.phoneNumber());
            customer.setAddress(command.address());

            byte[] salt = passwordHashService.generateSalt();
            byte[] hash = passwordHashService.hashPassword(command.password(), salt);

            ApplicationUser user = new ApplicationUser(
                    customer.getId(),
                    command.email(),
                    hash,
                    salt,
                    UserRole.CUSTOMER
            );

            uow.beginTransaction();
            uow.corporateCustomers().save(customer);
            uow.applicationUsers().save(user);
            uow.commit();

            return new CreatedCorporateCustomerResponse(
                    customer.getId(),
                    customer.getCompanyName(),
                    customer.getTaxNumber(),
                    "Kurumsal müşteri başarıyla oluşturuldu"
            );
        }
    }
}
