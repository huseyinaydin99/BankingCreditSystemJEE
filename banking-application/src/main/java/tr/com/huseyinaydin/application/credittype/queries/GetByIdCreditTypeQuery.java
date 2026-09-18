package tr.com.huseyinaydin.application.credittype.queries;

import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.credittype.dtos.CreditTypeResponse;
import tr.com.huseyinaydin.application.mapping.CreditTypeMapper;
import tr.com.huseyinaydin.domain.repositories.ICreditTypeRepository;
import tr.com.huseyinaydin.sharedkernel.exception.NotFoundException;
import tr.com.huseyinaydin.sharedkernel.messaging.IQuery;
import tr.com.huseyinaydin.sharedkernel.messaging.IQueryHandler;

import java.util.UUID;


public record GetByIdCreditTypeQuery(
        UUID id
) implements IQuery<CreditTypeResponse> {

    @Component
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public static class Handler
            implements IQueryHandler<GetByIdCreditTypeQuery, CreditTypeResponse> {

        private final ICreditTypeRepository creditTypes;
        private final CreditTypeMapper mapper;

        public Handler(ICreditTypeRepository creditTypes, CreditTypeMapper mapper) {
            this.creditTypes = creditTypes;
            this.mapper = mapper;
        }

        @Override
        public CreditTypeResponse handle(GetByIdCreditTypeQuery query) {
            return creditTypes
                    .findById(query.id())
                    .map(mapper::toResponse)
                    .orElseThrow(() -> new NotFoundException("CREDIT_TYPE", query.id().toString()));
        }
    }
}
