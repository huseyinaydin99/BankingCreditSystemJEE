package tr.com.huseyinaydin.application.customers.commands;

import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.customers.dtos.DeletedCorporateCustomerResponse;
import tr.com.huseyinaydin.application.customers.rules.CorporateCustomerBusinessRules;
import tr.com.huseyinaydin.application.ports.IUnitOfWork;
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

        private final IUnitOfWork uow;
        private final CorporateCustomerBusinessRules businessRules;

        public Handler(IUnitOfWork uow, CorporateCustomerBusinessRules businessRules) {
            this.uow = uow;
            this.businessRules = businessRules;
        }

        @Override
        public DeletedCorporateCustomerResponse handle(DeleteCorporateCustomerCommand command) {
            businessRules.customerShouldExistWhenRequested(command.id());

            CorporateCustomer customer = uow.corporateCustomers()
                    .findById(command.id())
                    .orElseThrow();

            customer.setActive(false);

            uow.beginTransaction();
            uow.corporateCustomers().delete(customer, command.permanent());
            uow.commit();

            return new DeletedCorporateCustomerResponse(
                    customer.getId(),
                    true,
                    "Kurumsal müşteri başarıyla silindi"
            );
        }
    }
}
