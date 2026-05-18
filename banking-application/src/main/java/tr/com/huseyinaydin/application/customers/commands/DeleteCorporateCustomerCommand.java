package tr.com.huseyinaydin.application.customers.commands;

import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.customers.rules.CorporateCustomerBusinessRules;
import tr.com.huseyinaydin.application.ports.IUnitOfWork;
import tr.com.huseyinaydin.domain.customer.CorporateCustomer;
import tr.com.huseyinaydin.sharedkernel.messaging.ICommand;
import tr.com.huseyinaydin.sharedkernel.messaging.ICommandHandler;

import java.time.LocalDateTime;
import java.util.UUID;

public record DeleteCorporateCustomerCommand(
        UUID id,
        boolean permanent
) implements ICommand<DeleteCorporateCustomerCommand.Response> {

    public DeleteCorporateCustomerCommand(UUID id) {
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
            implements ICommandHandler<DeleteCorporateCustomerCommand, Response> {

        private final IUnitOfWork uow;
        private final CorporateCustomerBusinessRules businessRules;

        public Handler(IUnitOfWork uow, CorporateCustomerBusinessRules businessRules) {
            this.uow = uow;
            this.businessRules = businessRules;
        }

        @Override
        public Response handle(DeleteCorporateCustomerCommand command) {
            businessRules.customerShouldExistWhenRequested(command.id());

            CorporateCustomer customer = uow.corporateCustomers()
                    .findById(command.id())
                    .orElseThrow();

            customer.setActive(false);

            uow.beginTransaction();
            uow.corporateCustomers().delete(customer, command.permanent());
            uow.commit();

            return new Response(customer.getId(), true, customer.getDeletedDate());
        }
    }
}
