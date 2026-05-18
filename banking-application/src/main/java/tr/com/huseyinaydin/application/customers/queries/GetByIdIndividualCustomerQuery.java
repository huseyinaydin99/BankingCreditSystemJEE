package tr.com.huseyinaydin.application.customers.queries;

import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.customers.dtos.IndividualCustomerResponse;
import tr.com.huseyinaydin.application.mapping.IndividualCustomerMapper;
import tr.com.huseyinaydin.application.ports.IUnitOfWork;
import tr.com.huseyinaydin.sharedkernel.exception.NotFoundException;
import tr.com.huseyinaydin.sharedkernel.messaging.IQuery;
import tr.com.huseyinaydin.sharedkernel.messaging.IQueryHandler;

import java.util.UUID;

public record GetByIdIndividualCustomerQuery(
        UUID id
) implements IQuery<IndividualCustomerResponse> {

    @Component
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public static class Handler
            implements IQueryHandler<GetByIdIndividualCustomerQuery, IndividualCustomerResponse> {

        private final IUnitOfWork uow;
        private final IndividualCustomerMapper mapper;

        public Handler(IUnitOfWork uow, IndividualCustomerMapper mapper) {
            this.uow = uow;
            this.mapper = mapper;
        }

        @Override
        public IndividualCustomerResponse handle(GetByIdIndividualCustomerQuery query) {
            return uow.individualCustomers()
                    .findById(query.id())
                    .map(mapper::toResponse)
                    .orElseThrow(() -> new NotFoundException("INDIVIDUAL_CUSTOMER", query.id().toString()));
        }
    }
}
