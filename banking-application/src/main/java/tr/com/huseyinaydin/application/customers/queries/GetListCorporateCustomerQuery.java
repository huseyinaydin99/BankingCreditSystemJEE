package tr.com.huseyinaydin.application.customers.queries;

import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.customers.dtos.CorporateCustomerResponse;
import tr.com.huseyinaydin.application.mapping.CorporateCustomerMapper;
import tr.com.huseyinaydin.application.ports.IUnitOfWork;
import tr.com.huseyinaydin.domain.repositories.Specification;
import tr.com.huseyinaydin.sharedkernel.messaging.IQuery;
import tr.com.huseyinaydin.sharedkernel.messaging.IQueryHandler;
import tr.com.huseyinaydin.sharedkernel.pagination.PageableQuery;
import tr.com.huseyinaydin.sharedkernel.pagination.Paginate;
import tr.com.huseyinaydin.sharedkernel.pagination.PaginationRequest;

public record GetListCorporateCustomerQuery(
        int pageIndex,
        int pageSize
) implements IQuery<Paginate<CorporateCustomerResponse>>, PageableQuery {

    @Override public int getPageIndex() { return pageIndex; }
    @Override public int getPageSize()  { return pageSize; }

    @Component
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public static class Handler
            implements IQueryHandler<GetListCorporateCustomerQuery, Paginate<CorporateCustomerResponse>> {

        private final IUnitOfWork uow;
        private final CorporateCustomerMapper mapper;

        public Handler(IUnitOfWork uow, CorporateCustomerMapper mapper) {
            this.uow = uow;
            this.mapper = mapper;
        }

        @Override
        public Paginate<CorporateCustomerResponse> handle(GetListCorporateCustomerQuery query) {
            PaginationRequest pagination = new PaginationRequest(query.pageIndex(), query.pageSize());
            return mapper.toResponsePage(
                    uow.corporateCustomers().findAll(Specification.all(), pagination)
            );
        }
    }
}
