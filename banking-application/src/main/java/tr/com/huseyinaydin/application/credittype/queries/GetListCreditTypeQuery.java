package tr.com.huseyinaydin.application.credittype.queries;

import jakarta.persistence.criteria.Predicate;
import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.credittype.dtos.CreditTypeResponse;
import tr.com.huseyinaydin.application.mapping.CreditTypeMapper;
import tr.com.huseyinaydin.application.ports.IUnitOfWork;
import tr.com.huseyinaydin.domain.credittype.CreditType;
import tr.com.huseyinaydin.domain.enums.CustomerType;
import tr.com.huseyinaydin.domain.repositories.Specification;
import tr.com.huseyinaydin.sharedkernel.messaging.IQuery;
import tr.com.huseyinaydin.sharedkernel.messaging.IQueryHandler;
import tr.com.huseyinaydin.sharedkernel.pagination.PageableQuery;
import tr.com.huseyinaydin.sharedkernel.pagination.Paginate;
import tr.com.huseyinaydin.sharedkernel.pagination.PaginationRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Kredi türlerini sayfalı listeler. Hiyerarşi filtresi:
 * {@code parentCreditTypeId == null} → yalnızca kök türler (ebeveyni olmayanlar);
 * değer verilirse → belirtilen ebeveynin alt türleri. İsteğe bağlı {@code customerType}
 * filtresiyle birlikte uygulanabilir.
 */
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

        private final IUnitOfWork uow;
        private final CreditTypeMapper mapper;

        public Handler(IUnitOfWork uow, CreditTypeMapper mapper) {
            this.uow = uow;
            this.mapper = mapper;
        }

        @Override
        public Paginate<CreditTypeResponse> handle(GetListCreditTypeQuery query) {
            PaginationRequest pagination = new PaginationRequest(query.pageIndex(), query.pageSize());

            // deletedDate filtresi findAll tarafından otomatik uygulanır.
            Specification<CreditType> spec = (root, cq, cb) -> {
                List<Predicate> predicates = new ArrayList<>();

                if (query.parentCreditTypeId() == null) {
                    predicates.add(cb.isNull(root.get("parentCreditType")));
                } else {
                    predicates.add(cb.equal(
                            root.get("parentCreditType").get("id"), query.parentCreditTypeId()));
                }

                if (query.customerType() != null) {
                    predicates.add(cb.equal(root.get("customerType"), query.customerType()));
                }

                return cb.and(predicates.toArray(new Predicate[0]));
            };

            return mapper.toResponsePage(
                    uow.creditTypes().findAll(spec, pagination));
        }
    }
}
