package tr.com.huseyinaydin.application.customers.queries;

import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.customers.dtos.IndividualCustomerResponse;
import tr.com.huseyinaydin.application.mapping.IndividualCustomerMapper;
import tr.com.huseyinaydin.domain.repositories.IIndividualCustomerRepository;
import tr.com.huseyinaydin.domain.repositories.Specification;
import tr.com.huseyinaydin.sharedkernel.messaging.IQuery;
import tr.com.huseyinaydin.sharedkernel.messaging.IQueryHandler;
import tr.com.huseyinaydin.sharedkernel.pagination.PageableQuery;
import tr.com.huseyinaydin.sharedkernel.pagination.Paginate;
import tr.com.huseyinaydin.sharedkernel.pagination.PaginationRequest;

public record GetListIndividualCustomerQuery(
        int pageIndex,
        int pageSize
) implements IQuery<Paginate<IndividualCustomerResponse>>, PageableQuery {

    @Override public int getPageIndex() { return pageIndex; }
    @Override public int getPageSize()  { return pageSize; }

    @Component
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public static class Handler
            implements IQueryHandler<GetListIndividualCustomerQuery, Paginate<IndividualCustomerResponse>> {

        private final IIndividualCustomerRepository individualCustomers;
        private final IndividualCustomerMapper mapper;

        public Handler(IIndividualCustomerRepository individualCustomers, IndividualCustomerMapper mapper) {
            this.individualCustomers = individualCustomers;
            this.mapper = mapper;
        }

        @Override
        public Paginate<IndividualCustomerResponse> handle(GetListIndividualCustomerQuery query) {
            PaginationRequest pagination = new PaginationRequest(query.pageIndex(), query.pageSize());
            return mapper.toResponsePage(
                    individualCustomers.findAll(Specification.all(), pagination)
            );
        }
    }
}
