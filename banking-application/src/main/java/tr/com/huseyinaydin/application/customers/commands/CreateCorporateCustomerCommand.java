package tr.com.huseyinaydin.application.customers.commands;

import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.customers.rules.CorporateCustomerBusinessRules;
import tr.com.huseyinaydin.application.ports.IMapper;
import tr.com.huseyinaydin.application.ports.IPasswordHashService;
import tr.com.huseyinaydin.application.ports.IUnitOfWork;
import tr.com.huseyinaydin.domain.customer.CorporateCustomer;
import tr.com.huseyinaydin.domain.enums.UserRole;
import tr.com.huseyinaydin.domain.user.ApplicationUser;
import tr.com.huseyinaydin.sharedkernel.messaging.ICommand;
import tr.com.huseyinaydin.sharedkernel.messaging.ICommandHandler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreateCorporateCustomerCommand(
        String companyName,
        String taxNumber,
        String taxOffice,
        String companyRegistrationNumber,
        String authorizedPersonName,
        LocalDate companyFoundationDate,
        String phoneNumber,
        String email,
        String address,
        String password
) implements ICommand<CreateCorporateCustomerCommand.Response> {

    public record Response(
            UUID id,
            String companyName,
            String taxNumber,
            String email,
            LocalDateTime createdDate
    ) {}

    @Component
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public static class Handler
            implements ICommandHandler<CreateCorporateCustomerCommand, Response> {

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
        public Response handle(CreateCorporateCustomerCommand command) {
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

            return new Response(
                    customer.getId(),
                    customer.getCompanyName(),
                    customer.getTaxNumber(),
                    customer.getEmail(),
                    customer.getCreatedDate()
            );
        }
    }
}
