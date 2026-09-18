package tr.com.huseyinaydin.application.customers.commands;

import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.customers.dtos.DeletedCorporateCustomerResponse;
import tr.com.huseyinaydin.application.customers.rules.CorporateCustomerBusinessRules;
import tr.com.huseyinaydin.domain.repositories.ICorporateCustomerRepository;
import tr.com.huseyinaydin.domain.customer.CorporateCustomer;
import tr.com.huseyinaydin.sharedkernel.messaging.ICommand;
import tr.com.huseyinaydin.sharedkernel.messaging.ICommandHandler;

import java.util.UUID;

public record DeleteCorporateCustomerCommand(
        UUID id,
        boolean permanent
) implements ICommand<DeletedCorporateCustomerResponse> {

    public DeleteCorporateCustomerCommand(UUID id) {
        this(id, false);
    }

    @Component
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public static class Handler
            implements ICommandHandler<DeleteCorporateCustomerCommand, DeletedCorporateCustomerResponse> {

        private final ICorporateCustomerRepository corporateCustomers;
        private final CorporateCustomerBusinessRules businessRules;

        public Handler(ICorporateCustomerRepository corporateCustomers, CorporateCustomerBusinessRules businessRules) {
            this.corporateCustomers = corporateCustomers;
            this.businessRules = businessRules;
        }

        @Override
        public DeletedCorporateCustomerResponse handle(DeleteCorporateCustomerCommand command) {
            businessRules.customerShouldExistWhenRequested(command.id());

            CorporateCustomer customer = corporateCustomers
                    .findById(command.id())
                    .orElseThrow();

            customer.deactivate();

            corporateCustomers.delete(customer, command.permanent());

            return new DeletedCorporateCustomerResponse(
                    customer.getId(),
                    true,
                    "Kurumsal müşteri başarıyla silindi"
            );
        }
    }
}
