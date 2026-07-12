package tr.com.huseyinaydin.application.credittype.queries;

import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.credittype.dtos.CreditTypeResponse;
import tr.com.huseyinaydin.application.mapping.CreditTypeMapper;
import tr.com.huseyinaydin.application.ports.IUnitOfWork;
import tr.com.huseyinaydin.sharedkernel.exception.NotFoundException;
import tr.com.huseyinaydin.sharedkernel.messaging.IQuery;
import tr.com.huseyinaydin.sharedkernel.messaging.IQueryHandler;

import java.util.UUID;

/**
 * Tek bir kredi türünü id ile getirir; bulunamazsa {@link NotFoundException}.
 * Mimari için {@code GetByIdIndividualCustomerQuery} örnek alınmıştır.
 */
public record GetByIdCreditTypeQuery(
        UUID id
) implements IQuery<CreditTypeResponse> {

    @Component
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public static class Handler
            implements IQueryHandler<GetByIdCreditTypeQuery, CreditTypeResponse> {

        private final IUnitOfWork uow;
        private final CreditTypeMapper mapper;

        public Handler(IUnitOfWork uow, CreditTypeMapper mapper) {
            this.uow = uow;
            this.mapper = mapper;
        }

        @Override
        public CreditTypeResponse handle(GetByIdCreditTypeQuery query) {
            return uow.creditTypes()
                    .findById(query.id())
                    .map(mapper::toResponse)
                    .orElseThrow(() -> new NotFoundException("CREDIT_TYPE", query.id().toString()));
        }
    }
}
