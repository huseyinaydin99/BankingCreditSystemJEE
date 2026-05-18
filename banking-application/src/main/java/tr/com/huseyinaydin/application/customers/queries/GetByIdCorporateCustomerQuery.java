package tr.com.huseyinaydin.application.customers.queries;

import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.customers.dtos.CorporateCustomerResponse;
import tr.com.huseyinaydin.application.mapping.CorporateCustomerMapper;
import tr.com.huseyinaydin.application.ports.IUnitOfWork;
import tr.com.huseyinaydin.sharedkernel.exception.NotFoundException;
import tr.com.huseyinaydin.sharedkernel.messaging.IQuery;
import tr.com.huseyinaydin.sharedkernel.messaging.IQueryHandler;

import java.util.UUID;

public record GetByIdCorporateCustomerQuery(
        UUID id
) implements IQuery<CorporateCustomerResponse> {

    @Component
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public static class Handler
            implements IQueryHandler<GetByIdCorporateCustomerQuery, CorporateCustomerResponse> {

        private final IUnitOfWork uow;
        private final CorporateCustomerMapper mapper;

        public Handler(IUnitOfWork uow, CorporateCustomerMapper mapper) {
            this.uow = uow;
            this.mapper = mapper;
        }

        @Override
        public CorporateCustomerResponse handle(GetByIdCorporateCustomerQuery query) {
            return uow.corporateCustomers()
                    .findById(query.id())
                    .map(mapper::toResponse)
                    .orElseThrow(() -> new NotFoundException("CORPORATE_CUSTOMER", query.id().toString()));
        }
    }
}
