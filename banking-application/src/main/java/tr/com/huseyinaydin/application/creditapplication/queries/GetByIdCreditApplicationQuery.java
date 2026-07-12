package tr.com.huseyinaydin.application.creditapplication.queries;

import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.creditapplication.dtos.CreditApplicationResponse;
import tr.com.huseyinaydin.application.creditapplication.rules.CreditApplicationBusinessRules;
import tr.com.huseyinaydin.application.mapping.CreditApplicationMapper;
import tr.com.huseyinaydin.application.ports.IUnitOfWork;
import tr.com.huseyinaydin.domain.creditapplication.CreditApplication;
import tr.com.huseyinaydin.domain.credittype.CreditType;
import tr.com.huseyinaydin.sharedkernel.exception.NotFoundException;
import tr.com.huseyinaydin.sharedkernel.messaging.IQuery;
import tr.com.huseyinaydin.sharedkernel.messaging.IQueryHandler;

import java.util.UUID;

/**
 * Tek bir kredi başvurusunu id ile getirir. Bulunamazsa {@link NotFoundException};
 * kullanıcı başvuruya yetkili değilse {@code AuthorizationException} (yetki kontrolü
 * {@link CreditApplicationBusinessRules#userCanAccessApplication} üzerinden).
 */
public record GetByIdCreditApplicationQuery(
        UUID id
) implements IQuery<CreditApplicationResponse> {

    @Component
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public static class Handler
            implements IQueryHandler<GetByIdCreditApplicationQuery, CreditApplicationResponse> {

        private final IUnitOfWork uow;
        private final CreditApplicationMapper mapper;
        private final CreditApplicationBusinessRules rules;

        public Handler(IUnitOfWork uow,
                       CreditApplicationMapper mapper,
                       CreditApplicationBusinessRules rules) {
            this.uow = uow;
            this.mapper = mapper;
            this.rules = rules;
        }

        @Override
        public CreditApplicationResponse handle(GetByIdCreditApplicationQuery query) {
            CreditApplication application = uow.creditApplications()
                    .findById(query.id())
                    .orElseThrow(() -> new NotFoundException("CREDIT_APPLICATION", query.id().toString()));

            rules.userCanAccessApplication(application);

            CreditType creditType = uow.creditTypes()
                    .findById(application.getCreditTypeId())
                    .orElse(null);

            return mapper.toResponse(application, creditType);
        }
    }
}
