package tr.com.huseyinaydin.application.customers.commands;

import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.customers.rules.IndividualCustomerBusinessRules;
import tr.com.huseyinaydin.application.ports.IMapper;
import tr.com.huseyinaydin.application.ports.IPasswordHashService;
import tr.com.huseyinaydin.application.ports.IUnitOfWork;
import tr.com.huseyinaydin.domain.customer.IndividualCustomer;
import tr.com.huseyinaydin.domain.enums.UserRole;
import tr.com.huseyinaydin.domain.user.ApplicationUser;
import tr.com.huseyinaydin.sharedkernel.messaging.ICommand;
import tr.com.huseyinaydin.sharedkernel.messaging.ICommandHandler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreateIndividualCustomerCommand(
        String firstName,
        String lastName,
        String nationalId,
        LocalDate dateOfBirth,
        String motherName,
        String fatherName,
        String phoneNumber,
        String email,
        String address,
        String password
) implements ICommand<CreateIndividualCustomerCommand.Response> {

    public record Response(
            UUID id,
            String fullName,
            String email,
            String nationalId,
            LocalDateTime createdDate
    ) {}

    @Component
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public static class Handler
            implements ICommandHandler<CreateIndividualCustomerCommand, Response> {

        private final IUnitOfWork uow;
        private final IndividualCustomerBusinessRules businessRules;
        private final IPasswordHashService passwordHashService;
        private final IMapper mapper;

        public Handler(IUnitOfWork uow,
                       IndividualCustomerBusinessRules businessRules,
                       IPasswordHashService passwordHashService,
                       IMapper mapper) {
            this.uow = uow;
            this.businessRules = businessRules;
            this.passwordHashService = passwordHashService;
            this.mapper = mapper;
        }

        @Override
        public Response handle(CreateIndividualCustomerCommand command) {
            businessRules.nationalIdCannotBeDuplicatedWhenInserted(command.nationalId());

            IndividualCustomer customer = new IndividualCustomer(
                    command.firstName(),
                    command.lastName(),
                    command.nationalId(),
                    command.email()
            );
            customer.setDateOfBirth(command.dateOfBirth());
            customer.setMotherName(command.motherName());
            customer.setFatherName(command.fatherName());
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
            uow.individualCustomers().save(customer);
            uow.applicationUsers().save(user);
            uow.commit();

            return new Response(
                    customer.getId(),
                    customer.getFullName(),
                    customer.getEmail(),
                    customer.getNationalId(),
                    customer.getCreatedDate()
            );
        }
    }
}
