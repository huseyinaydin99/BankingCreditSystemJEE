package tr.com.huseyinaydin.application.customers.queries;

import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.customers.dtos.IndividualCustomerResponse;
import tr.com.huseyinaydin.application.ports.read.IIndividualCustomerReadService;
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

        private final IIndividualCustomerReadService readService;

        public Handler(IIndividualCustomerReadService readService) {
            this.readService = readService;
        }

        @Override
        @org.springframework.cache.annotation.Cacheable(value = "customers", key = "#query.id()")
        public IndividualCustomerResponse handle(GetByIdIndividualCustomerQuery query) {
            return readService.getById(query.id())
                    .orElseThrow(() -> new NotFoundException("INDIVIDUAL_CUSTOMER", query.id().toString()));
        }
    }
}
