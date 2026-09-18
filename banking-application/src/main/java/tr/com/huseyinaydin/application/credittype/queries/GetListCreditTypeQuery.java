package tr.com.huseyinaydin.application.credittype.queries;

import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.credittype.dtos.CreditTypeResponse;
import tr.com.huseyinaydin.application.ports.read.ICreditTypeReadService;
import tr.com.huseyinaydin.domain.enums.CustomerType;
import tr.com.huseyinaydin.sharedkernel.messaging.IQuery;
import tr.com.huseyinaydin.sharedkernel.messaging.IQueryHandler;
import tr.com.huseyinaydin.sharedkernel.pagination.PageableQuery;
import tr.com.huseyinaydin.sharedkernel.pagination.Paginate;

import java.util.UUID;


public record GetListCreditTypeQuery(
        CustomerType customerType,
        UUID parentCreditTypeId,
        int pageIndex,
        int pageSize
) implements IQuery<Paginate<CreditTypeResponse>>, PageableQuery {

    @Override public int getPageIndex() { return pageIndex; }
    @Override public int getPageSize()  { return pageSize; }

    @Component
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public static class Handler
            implements IQueryHandler<GetListCreditTypeQuery, Paginate<CreditTypeResponse>> {

        private final ICreditTypeReadService readService;

        public Handler(ICreditTypeReadService readService) {
            this.readService = readService;
        }

        @Override
        @org.springframework.cache.annotation.Cacheable("creditTypes")
        public Paginate<CreditTypeResponse> handle(GetListCreditTypeQuery query) {
            return readService.getList(
                    query.customerType(),
                    query.parentCreditTypeId(),
                    query.pageIndex(),
                    query.pageSize()
            );
        }
    }
}
