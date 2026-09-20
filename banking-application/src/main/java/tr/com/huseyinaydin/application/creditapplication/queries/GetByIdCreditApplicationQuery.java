package tr.com.huseyinaydin.application.creditapplication.queries;

import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.creditapplication.dtos.CreditApplicationResponse;
import tr.com.huseyinaydin.application.creditapplication.rules.CreditApplicationBusinessRules;
import tr.com.huseyinaydin.application.ports.read.ICreditApplicationReadService;
import tr.com.huseyinaydin.sharedkernel.exception.NotFoundException;
import tr.com.huseyinaydin.sharedkernel.messaging.IQuery;
import tr.com.huseyinaydin.sharedkernel.messaging.IQueryHandler;

import java.util.UUID;

public record GetByIdCreditApplicationQuery(
        UUID id
) implements IQuery<CreditApplicationResponse> {

    @Component
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public static class Handler
            implements IQueryHandler<GetByIdCreditApplicationQuery, CreditApplicationResponse> {

        private final ICreditApplicationReadService readService;
        private final CreditApplicationBusinessRules rules;

        public Handler(ICreditApplicationReadService readService,
                       CreditApplicationBusinessRules rules) {
            this.readService = readService;
            this.rules = rules;
        }

        @Override
        public CreditApplicationResponse handle(GetByIdCreditApplicationQuery query) {
            CreditApplicationResponse response = readService.getById(query.id());
            
            if (response == null) {
                throw new NotFoundException("CREDIT_APPLICATION", query.id().toString());
            }

            rules.userCanAccessApplicationByCustomerId(response.customerId());

            return response;
        }
    }
}
