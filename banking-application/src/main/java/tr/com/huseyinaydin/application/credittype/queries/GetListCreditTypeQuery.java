package tr.com.huseyinaydin.application.credittype.queries;

import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.credittype.dtos.CreditTypeResponse;
import tr.com.huseyinaydin.application.mapping.CreditTypeMapper;
import tr.com.huseyinaydin.application.ports.IUnitOfWork;
import tr.com.huseyinaydin.domain.enums.CustomerType;
import tr.com.huseyinaydin.domain.repositories.Specification;
import tr.com.huseyinaydin.sharedkernel.messaging.IQuery;
import tr.com.huseyinaydin.sharedkernel.messaging.IQueryHandler;
import tr.com.huseyinaydin.sharedkernel.pagination.PageableQuery;
import tr.com.huseyinaydin.sharedkernel.pagination.Paginate;
import tr.com.huseyinaydin.sharedkernel.pagination.PaginationRequest;

public record GetListCreditTypeQuery(
        CustomerType customerType,
        int pageIndex,
        int pageSize
) implements IQuery<Paginate<CreditTypeResponse>>, PageableQuery {

    @Override public int getPageIndex() { return pageIndex; }
    @Override public int getPageSize()  { return pageSize; }

    @Component
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public static class Handler
            implements IQueryHandler<GetListCreditTypeQuery, Paginate<CreditTypeResponse>> {

        private final IUnitOfWork uow;
        private final CreditTypeMapper mapper;

        public Handler(IUnitOfWork uow, CreditTypeMapper mapper) {
            this.uow = uow;
            this.mapper = mapper;
        }

        @Override
        public Paginate<CreditTypeResponse> handle(GetListCreditTypeQuery query) {
            PaginationRequest pagination = new PaginationRequest(query.pageIndex(), query.pageSize());

            if (query.customerType() != null) {
                return mapper.toResponsePage(
                        uow.creditTypes().findByCustomerType(query.customerType(), pagination)
                );
            }

            return mapper.toResponsePage(
                    uow.creditTypes().findAll(Specification.all(), pagination)
            );
        }
    }
}
