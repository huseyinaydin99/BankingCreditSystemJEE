package tr.com.huseyinaydin.application.creditapplication.queries;

import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.creditapplication.dtos.CreditApplicationResponse;
import tr.com.huseyinaydin.application.mapping.CreditApplicationMapper;
import tr.com.huseyinaydin.application.ports.IUnitOfWork;
import tr.com.huseyinaydin.domain.creditapplication.CreditApplication;
import tr.com.huseyinaydin.domain.credittype.CreditType;
import tr.com.huseyinaydin.sharedkernel.messaging.IQuery;
import tr.com.huseyinaydin.sharedkernel.messaging.IQueryHandler;
import tr.com.huseyinaydin.sharedkernel.pagination.PageableQuery;
import tr.com.huseyinaydin.sharedkernel.pagination.Paginate;
import tr.com.huseyinaydin.sharedkernel.pagination.PaginationRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record GetListByCustomerCreditApplicationQuery(
        UUID customerId,
        int pageIndex,
        int pageSize
) implements IQuery<Paginate<CreditApplicationResponse>>, PageableQuery {

    @Override public int getPageIndex() { return pageIndex; }
    @Override public int getPageSize()  { return pageSize; }

    @Component
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public static class Handler
            implements IQueryHandler<GetListByCustomerCreditApplicationQuery, Paginate<CreditApplicationResponse>> {

        private final IUnitOfWork uow;
        private final CreditApplicationMapper mapper;

        public Handler(IUnitOfWork uow, CreditApplicationMapper mapper) {
            this.uow = uow;
            this.mapper = mapper;
        }

        @Override
        public Paginate<CreditApplicationResponse> handle(GetListByCustomerCreditApplicationQuery query) {
            PaginationRequest pagination = new PaginationRequest(query.pageIndex(), query.pageSize());
            Paginate<CreditApplication> page = uow.creditApplications()
                    .findByCustomerId(query.customerId(), pagination);

            Set<UUID> creditTypeIds = page.getItems().stream()
                    .map(CreditApplication::getCreditTypeId)
                    .collect(Collectors.toSet());

            Map<UUID, CreditType> creditTypeMap = new HashMap<>();
            for (UUID id : creditTypeIds) {
                uow.creditTypes().findById(id)
                        .ifPresent(ct -> creditTypeMap.put(ct.getId(), ct));
            }

            List<CreditApplicationResponse> responses = page.getItems().stream()
                    .map(app -> mapper.toResponse(app, creditTypeMap.get(app.getCreditTypeId())))
                    .collect(Collectors.toList());

            return new Paginate<>(responses, page.getPageIndex(), page.getPageSize(), page.getTotalCount());
        }
    }
}
