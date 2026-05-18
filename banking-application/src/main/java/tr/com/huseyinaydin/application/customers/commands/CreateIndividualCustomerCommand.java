package tr.com.huseyinaydin.application.customers.commands;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.customers.dtos.CreatedIndividualCustomerResponse;
import tr.com.huseyinaydin.application.customers.rules.IndividualCustomerBusinessRules;
import tr.com.huseyinaydin.application.ports.IMapper;
import tr.com.huseyinaydin.application.ports.IPasswordHashService;
import tr.com.huseyinaydin.application.ports.IUnitOfWork;
import tr.com.huseyinaydin.application.validation.constraints.PhoneNumber;
import tr.com.huseyinaydin.application.validation.constraints.TurkishNationalId;
import tr.com.huseyinaydin.domain.customer.IndividualCustomer;
import tr.com.huseyinaydin.domain.enums.UserRole;
import tr.com.huseyinaydin.domain.user.ApplicationUser;
import tr.com.huseyinaydin.sharedkernel.messaging.ICommand;
import tr.com.huseyinaydin.sharedkernel.messaging.ICommandHandler;

import java.time.LocalDate;

public record CreateIndividualCustomerCommand(
        @NotBlank @Size(max = 50) String firstName,
        @NotBlank @Size(max = 50) String lastName,
        @NotBlank @TurkishNationalId String nationalId,
        @NotNull LocalDate dateOfBirth,
        String motherName,
        String fatherName,
        @NotBlank @PhoneNumber String phoneNumber,
        @NotBlank @Email String email,
        String address,
        @NotBlank String password
) implements ICommand<CreatedIndividualCustomerResponse> {

    @Component
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public static class Handler
            implements ICommandHandler<CreateIndividualCustomerCommand, CreatedIndividualCustomerResponse> {

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
        public CreatedIndividualCustomerResponse handle(CreateIndividualCustomerCommand command) {
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

            return new CreatedIndividualCustomerResponse(
                    customer.getId(),
                    customer.getFirstName(),
                    customer.getLastName(),
                    customer.getNationalId(),
                    customer.getEmail(),
                    "Bireysel müşteri başarıyla oluşturuldu"
            );
        }
    }
}
