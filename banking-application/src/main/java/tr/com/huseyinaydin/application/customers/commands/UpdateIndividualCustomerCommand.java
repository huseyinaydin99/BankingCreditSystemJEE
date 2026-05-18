package tr.com.huseyinaydin.application.customers.commands;

import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.customers.dtos.UpdatedIndividualCustomerResponse;
import tr.com.huseyinaydin.application.customers.rules.IndividualCustomerBusinessRules;
import tr.com.huseyinaydin.application.ports.IMapper;
import tr.com.huseyinaydin.application.ports.IUnitOfWork;
import tr.com.huseyinaydin.domain.customer.IndividualCustomer;
import tr.com.huseyinaydin.sharedkernel.messaging.ICommand;
import tr.com.huseyinaydin.sharedkernel.messaging.ICommandHandler;

import java.time.LocalDate;
import java.util.UUID;

public record UpdateIndividualCustomerCommand(
        UUID id,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String motherName,
        String fatherName,
        String phoneNumber,
        String email,
        String address
) implements ICommand<UpdatedIndividualCustomerResponse> {

    @Component
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public static class Handler
            implements ICommandHandler<UpdateIndividualCustomerCommand, UpdatedIndividualCustomerResponse> {

        private final IUnitOfWork uow;
        private final IndividualCustomerBusinessRules businessRules;
        private final IMapper mapper;

        public Handler(IUnitOfWork uow,
                       IndividualCustomerBusinessRules businessRules,
                       IMapper mapper) {
            this.uow = uow;
            this.businessRules = businessRules;
            this.mapper = mapper;
        }

        @Override
        public UpdatedIndividualCustomerResponse handle(UpdateIndividualCustomerCommand command) {
            businessRules.customerShouldExistWhenRequested(command.id());

            IndividualCustomer customer = uow.individualCustomers()
                    .findById(command.id())
                    .orElseThrow();

            businessRules.customerShouldBeActive(customer);

            customer.setFirstName(command.firstName());
            customer.setLastName(command.lastName());
            customer.setDateOfBirth(command.dateOfBirth());
            customer.setMotherName(command.motherName());
            customer.setFatherName(command.fatherName());
            customer.setPhoneNumber(command.phoneNumber());
            customer.setEmail(command.email());
            customer.setAddress(command.address());

            uow.beginTransaction();
            uow.individualCustomers().update(customer);
            uow.commit();

            return new UpdatedIndividualCustomerResponse(
                    customer.getId(),
                    customer.getFirstName(),
                    customer.getLastName(),
                    customer.getEmail(),
                    "Bireysel müşteri başarıyla güncellendi"
            );
        }
    }
}
