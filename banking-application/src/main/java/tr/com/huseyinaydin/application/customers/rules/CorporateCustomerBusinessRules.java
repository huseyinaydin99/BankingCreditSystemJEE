package tr.com.huseyinaydin.application.customers.rules;

import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.common.BankingErrorCodes;
import tr.com.huseyinaydin.domain.customer.Customer;
import tr.com.huseyinaydin.domain.repositories.ICorporateCustomerRepository;
import tr.com.huseyinaydin.sharedkernel.exception.BusinessException;
import tr.com.huseyinaydin.sharedkernel.exception.ConflictException;
import tr.com.huseyinaydin.sharedkernel.exception.NotFoundException;

import java.util.UUID;

@Component
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class CorporateCustomerBusinessRules {

    private final ICorporateCustomerRepository corporateCustomerRepository;

    public CorporateCustomerBusinessRules(ICorporateCustomerRepository corporateCustomerRepository) {
        this.corporateCustomerRepository = corporateCustomerRepository;
    }

    public void taxNumberCannotBeDuplicatedWhenInserted(String taxNumber) {
        corporateCustomerRepository.findByTaxNumber(taxNumber).ifPresent(existing -> {
            throw new ConflictException("taxNumber", taxNumber, "Bu Vergi Numarası zaten kayıtlı");
        });
    }

    public void tradeRegistrationNumberCannotBeDuplicatedWhenInserted(String tradeRegistrationNumber) {
        corporateCustomerRepository.findByTradeRegistrationNumber(tradeRegistrationNumber)
                .ifPresent(existing -> {
                    throw new ConflictException("tradeRegistrationNumber", tradeRegistrationNumber,
                            "Bu Ticaret Sicil Numarası zaten kayıtlı");
                });
    }

    public void tradeRegistrationNumberMustBeUniqueForUpdate(String tradeRegistrationNumber, UUID currentId) {
        corporateCustomerRepository.findByTradeRegistrationNumber(tradeRegistrationNumber)
                .filter(existing -> !existing.getId().equals(currentId))
                .ifPresent(existing -> {
                    throw new ConflictException("tradeRegistrationNumber", tradeRegistrationNumber,
                            "Bu Ticaret Sicil Numarası başka bir müşteriye ait");
                });
    }

    public void customerShouldExistWhenRequested(UUID id) {
        corporateCustomerRepository.findById(id).orElseThrow(() ->
                new NotFoundException("CORPORATE_CUSTOMER", id.toString()));
    }

    public void customerShouldBeActive(Customer customer) {
        if (!customer.isActive()) {
            throw new BusinessException("Müşteri aktif değil", BankingErrorCodes.CUSTOMER_INACTIVE);
        }
    }
}
