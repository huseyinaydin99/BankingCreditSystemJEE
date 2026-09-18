package tr.com.huseyinaydin.application.credittype.queries;

import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.credittype.dtos.CreditTypeResponse;
import tr.com.huseyinaydin.application.pipeline.ICacheableRequest;
import tr.com.huseyinaydin.application.ports.read.ICreditTypeReadService;
import tr.com.huseyinaydin.domain.enums.CustomerType;
import tr.com.huseyinaydin.sharedkernel.messaging.IQuery;
import tr.com.huseyinaydin.sharedkernel.messaging.IQueryHandler;
import tr.com.huseyinaydin.sharedkernel.pagination.PageableQuery;
import tr.com.huseyinaydin.sharedkernel.pagination.Paginate;

import java.time.Duration;
import java.util.UUID;


public record GetListCreditTypeQuery(
        CustomerType customerType,
        UUID parentCreditTypeId,
        int pageIndex,
        int pageSize
) implements IQuery<Paginate<CreditTypeResponse>>, PageableQuery, ICacheableRequest {

    @Override public int getPageIndex() { return pageIndex; }
    @Override public int getPageSize()  { return pageSize; }
    
    @Override public String getCacheName() { return "creditTypes"; }
    @Override public String getCacheKey() { 
        return "list_" + customerType + "_" + parentCreditTypeId + "_" + pageIndex + "_" + pageSize; 
    }
    @Override public Duration getTtl() { return Duration.ofMinutes(10); }

    @Component
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public static class Handler
            implements IQueryHandler<GetListCreditTypeQuery, Paginate<CreditTypeResponse>> {

        private final ICreditTypeReadService readService;

        public Handler(ICreditTypeReadService readService) {
            this.readService = readService;
        }

        @Override
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
