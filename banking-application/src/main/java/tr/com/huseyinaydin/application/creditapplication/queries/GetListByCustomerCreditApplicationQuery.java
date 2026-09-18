package tr.com.huseyinaydin.application.creditapplication.queries;

import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.creditapplication.dtos.CreditApplicationResponse;
import tr.com.huseyinaydin.application.ports.read.ICreditApplicationReadService;
import tr.com.huseyinaydin.sharedkernel.messaging.IQuery;
import tr.com.huseyinaydin.sharedkernel.messaging.IQueryHandler;
import tr.com.huseyinaydin.sharedkernel.pagination.PageableQuery;
import tr.com.huseyinaydin.sharedkernel.pagination.Paginate;

import java.util.UUID;

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

        private final ICreditApplicationReadService readService;

        public Handler(ICreditApplicationReadService readService) {
            this.readService = readService;
        }

        @Override
        public Paginate<CreditApplicationResponse> handle(GetListByCustomerCreditApplicationQuery query) {
            // CQRS'in read (okuma) mimarisine uygun olarak, Write model (UoW) kullanılmadan 
            // doğrudan Read Model (JdbcTemplate) üzerinden DTO çekiliyor. N+1 sorgu sorunu çözüldü!
            return readService.getListByCustomerId(query.customerId(), query.pageIndex(), query.pageSize());
        }
    }
}
