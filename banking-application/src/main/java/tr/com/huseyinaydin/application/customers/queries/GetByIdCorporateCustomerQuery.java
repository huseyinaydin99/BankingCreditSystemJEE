package tr.com.huseyinaydin.application.customers.queries;

import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.customers.dtos.CorporateCustomerResponse;
import tr.com.huseyinaydin.application.mapping.CorporateCustomerMapper;
import tr.com.huseyinaydin.domain.repositories.ICorporateCustomerRepository;
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

        private final ICorporateCustomerRepository corporateCustomers;
        private final CorporateCustomerMapper mapper;

        public Handler(ICorporateCustomerRepository corporateCustomers, CorporateCustomerMapper mapper) {
            this.corporateCustomers = corporateCustomers;
            this.mapper = mapper;
        }

        @Override
        public CorporateCustomerResponse handle(GetByIdCorporateCustomerQuery query) {
            return corporateCustomers
                    .findById(query.id())
                    .map(mapper::toResponse)
                    .orElseThrow(() -> new NotFoundException("CORPORATE_CUSTOMER", query.id().toString()));
        }
    }
}
