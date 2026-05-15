package tr.com.huseyinaydin.domain.repositories;

import tr.com.huseyinaydin.domain.credittype.CreditType;
import tr.com.huseyinaydin.domain.enums.CustomerType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ICreditTypeRepository {
    Optional<CreditType> findById(UUID id);
    CreditType save(CreditType creditType);
    void delete(UUID id);
    List<CreditType> findAll();
    List<CreditType> findByCustomerType(CustomerType customerType);
    List<CreditType> findByParentCreditTypeId(UUID parentCreditTypeId);
}
