package tr.com.huseyinaydin.application.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import tr.com.huseyinaydin.application.credittype.dtos.CreditTypeResponse;
import tr.com.huseyinaydin.domain.credittype.CreditType;
import tr.com.huseyinaydin.sharedkernel.pagination.Paginate;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(config = MapStructConfig.class)
public interface CreditTypeMapper {

    // Maps all flat fields; subCreditTypes handled in default toResponse()
    @Mapping(target = "parentCreditTypeId",
             expression = "java(entity.getParentCreditTypeId())")
    @Mapping(target = "subCreditTypes", ignore = true)
    CreditTypeResponse toResponseFlat(CreditType entity);

    // Recursive mapping: each sub-type is also mapped with its own children
    default CreditTypeResponse toResponse(CreditType entity) {
        List<CreditTypeResponse> subs =
                (entity.getSubCreditTypes() == null || entity.getSubCreditTypes().isEmpty())
                        ? Collections.emptyList()
                        : entity.getSubCreditTypes().stream()
                                .map(this::toResponse)
                                .collect(Collectors.toList());

        CreditTypeResponse flat = toResponseFlat(entity);
        return new CreditTypeResponse(
                flat.id(), flat.name(), flat.description(), flat.customerType(),
                flat.minAmount(), flat.maxAmount(), flat.minTerm(), flat.maxTerm(),
                flat.baseInterestRate(), flat.parentCreditTypeId(),
                subs,
                flat.createdDate()
        );
    }

    default Paginate<CreditTypeResponse> toResponsePage(Paginate<CreditType> page) {
        List<CreditTypeResponse> items = page.getItems().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return new Paginate<>(items, page.getPageIndex(), page.getPageSize(), page.getTotalCount());
    }
}
