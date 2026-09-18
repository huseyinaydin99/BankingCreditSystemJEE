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
) implements ICommand<UpdatedIndividualCustomerResponse>, tr.com.huseyinaydin.application.pipeline.ICacheEvictRequest {

    @Override public String getCacheName() { return "customers"; }
    @Override public String getCacheKey() { return id.toString(); }
    @Override public boolean isEvictAll() { return false; }

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

            
            
            
            
            customer.updatePersonalInfo(command.firstName(), command.lastName(), command.dateOfBirth(), command.motherName(), command.fatherName());
            
            
            customer.updateContactInfo(command.phoneNumber(), command.email(), command.address());

            uow.individualCustomers().update(customer);

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
