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
import tr.com.huseyinaydin.application.ports.PasswordHash;
import tr.com.huseyinaydin.domain.repositories.IIndividualCustomerRepository;
import tr.com.huseyinaydin.application.validation.constraints.PhoneNumber;
import tr.com.huseyinaydin.application.validation.constraints.TurkishNationalId;
import tr.com.huseyinaydin.domain.customer.IndividualCustomer;
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

        private final IIndividualCustomerRepository individualCustomerRepository;
        private final IndividualCustomerBusinessRules businessRules;
        private final IPasswordHashService passwordHashService;
        private final IMapper mapper;

        public Handler(IIndividualCustomerRepository individualCustomerRepository,
                       IndividualCustomerBusinessRules businessRules,
                       IPasswordHashService passwordHashService,
                       IMapper mapper) {
            this.individualCustomerRepository = individualCustomerRepository;
            this.businessRules = businessRules;
            this.passwordHashService = passwordHashService;
            this.mapper = mapper;
        }

        @Override
        public CreatedIndividualCustomerResponse handle(CreateIndividualCustomerCommand command) {
            businessRules.nationalIdCannotBeDuplicatedWhenInserted(command.nationalId());

            PasswordHash passwordHash = passwordHashService.createHash(command.password());

            IndividualCustomer customer = new IndividualCustomer(
                    command.firstName(),
                    command.lastName(),
                    command.nationalId(),
                    command.email(),
                    passwordHash.hash(),
                    passwordHash.salt()
            );
            customer.updatePersonalInfo(command.firstName(), command.lastName(), command.dateOfBirth(), command.motherName(), command.fatherName());
            customer.updateContactInfo(command.phoneNumber(), command.email(), command.address());

            // SRP and Aggregate Boundaries Fix: ApplicationUser creation is now strictly handled
            // by another event handler (via IndividualCustomerCreatedEvent). This CommandHandler 
            // only handles the IndividualCustomer aggregate root.
            individualCustomerRepository.save(customer);

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
