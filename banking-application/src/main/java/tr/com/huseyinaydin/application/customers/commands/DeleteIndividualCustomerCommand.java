package tr.com.huseyinaydin.application.customers.commands;

import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.customers.dtos.DeletedIndividualCustomerResponse;
import tr.com.huseyinaydin.application.customers.rules.IndividualCustomerBusinessRules;
import tr.com.huseyinaydin.application.ports.IUnitOfWork;
import tr.com.huseyinaydin.domain.customer.IndividualCustomer;
import tr.com.huseyinaydin.sharedkernel.messaging.ICommand;
import tr.com.huseyinaydin.sharedkernel.messaging.ICommandHandler;

import java.util.UUID;

public record DeleteIndividualCustomerCommand(
        UUID id,
        boolean permanent
) implements ICommand<DeletedIndividualCustomerResponse>, tr.com.huseyinaydin.application.pipeline.ICacheEvictRequest {

    @Override public String getCacheName() { return "customers"; }
    @Override public String getCacheKey() { return id.toString(); }
    @Override public boolean isEvictAll() { return false; }

    public DeleteIndividualCustomerCommand(UUID id) {
        this(id, false);
    }

    @Component
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public static class Handler
            implements ICommandHandler<DeleteIndividualCustomerCommand, DeletedIndividualCustomerResponse> {

        private final IUnitOfWork uow;
        private final IndividualCustomerBusinessRules businessRules;

        public Handler(IUnitOfWork uow, IndividualCustomerBusinessRules businessRules) {
            this.uow = uow;
            this.businessRules = businessRules;
        }

        @Override
        public DeletedIndividualCustomerResponse handle(DeleteIndividualCustomerCommand command) {
            businessRules.customerShouldExistWhenRequested(command.id());

            IndividualCustomer customer = uow.individualCustomers()
                    .findById(command.id())
                    .orElseThrow();

            customer.deactivate();

            uow.individualCustomers().delete(customer, command.permanent());

            return new DeletedIndividualCustomerResponse(
                    customer.getId(),
                    true,
                    "Bireysel müşteri başarıyla silindi"
            );
        }
    }
}
