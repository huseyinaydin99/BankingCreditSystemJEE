package tr.com.huseyinaydin.application.customers.commands;

import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.customers.rules.IndividualCustomerBusinessRules;
import tr.com.huseyinaydin.application.ports.IUnitOfWork;
import tr.com.huseyinaydin.domain.customer.IndividualCustomer;
import tr.com.huseyinaydin.sharedkernel.messaging.ICommand;
import tr.com.huseyinaydin.sharedkernel.messaging.ICommandHandler;

import java.time.LocalDateTime;
import java.util.UUID;

public record DeleteIndividualCustomerCommand(
        UUID id,
        boolean permanent
) implements ICommand<DeleteIndividualCustomerCommand.Response> {

    public DeleteIndividualCustomerCommand(UUID id) {
        this(id, false);
    }

    public record Response(
            UUID id,
            boolean deleted,
            LocalDateTime deletedDate
    ) {}

    @Component
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public static class Handler
            implements ICommandHandler<DeleteIndividualCustomerCommand, Response> {

        private final IUnitOfWork uow;
        private final IndividualCustomerBusinessRules businessRules;

        public Handler(IUnitOfWork uow, IndividualCustomerBusinessRules businessRules) {
            this.uow = uow;
            this.businessRules = businessRules;
        }

        @Override
        public Response handle(DeleteIndividualCustomerCommand command) {
            businessRules.customerShouldExistWhenRequested(command.id());

            IndividualCustomer customer = uow.individualCustomers()
                    .findById(command.id())
                    .orElseThrow();

            customer.setActive(false);

            uow.beginTransaction();
            uow.individualCustomers().delete(customer, command.permanent());
            uow.commit();

            return new Response(customer.getId(), true, customer.getDeletedDate());
        }
    }
}
