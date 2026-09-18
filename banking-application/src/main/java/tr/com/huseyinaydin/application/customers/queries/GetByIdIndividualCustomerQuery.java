package tr.com.huseyinaydin.application.customers.queries;

import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.customers.dtos.IndividualCustomerResponse;
import tr.com.huseyinaydin.application.pipeline.ICacheableRequest;
import tr.com.huseyinaydin.application.ports.read.IIndividualCustomerReadService;
import tr.com.huseyinaydin.sharedkernel.exception.NotFoundException;
import tr.com.huseyinaydin.sharedkernel.messaging.IQuery;
import tr.com.huseyinaydin.sharedkernel.messaging.IQueryHandler;

import java.time.Duration;
import java.util.UUID;

public record GetByIdIndividualCustomerQuery(
        UUID id
) implements IQuery<IndividualCustomerResponse>, ICacheableRequest {

    @Override public String getCacheName() { return "customers"; }
    @Override public String getCacheKey() { return id.toString(); }
    @Override public Duration getTtl() { return Duration.ofMinutes(5); }

    @Component
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public static class Handler
            implements IQueryHandler<GetByIdIndividualCustomerQuery, IndividualCustomerResponse> {

        private final IIndividualCustomerReadService readService;

        public Handler(IIndividualCustomerReadService readService) {
            this.readService = readService;
        }

        @Override
        public IndividualCustomerResponse handle(GetByIdIndividualCustomerQuery query) {
            return readService.getById(query.id())
                    .orElseThrow(() -> new NotFoundException("INDIVIDUAL_CUSTOMER", query.id().toString()));
        }
    }
}
