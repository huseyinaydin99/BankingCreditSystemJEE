package tr.com.huseyinaydin.application.customers.rules;

import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.common.BankingErrorCodes;
import tr.com.huseyinaydin.domain.customer.Customer;
import tr.com.huseyinaydin.domain.repositories.IIndividualCustomerRepository;
import tr.com.huseyinaydin.domain.repositories.Specification;
import tr.com.huseyinaydin.sharedkernel.exception.BusinessException;
import tr.com.huseyinaydin.sharedkernel.exception.ConflictException;
import tr.com.huseyinaydin.sharedkernel.exception.NotFoundException;

import java.util.UUID;

@Component
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class IndividualCustomerBusinessRules {

    private final IIndividualCustomerRepository individualCustomerRepository;

    public IndividualCustomerBusinessRules(IIndividualCustomerRepository individualCustomerRepository) {
        this.individualCustomerRepository = individualCustomerRepository;
    }

    public void nationalIdCannotBeDuplicatedWhenInserted(String nationalId) {
        individualCustomerRepository.findByNationalId(nationalId).ifPresent(existing -> {
            throw new ConflictException("nationalId", nationalId, "Bu TC Kimlik No zaten kayıtlı");
        });
    }

    public void customerShouldExistWhenRequested(UUID id) {
        individualCustomerRepository.findById(id).orElseThrow(() ->
                new NotFoundException("INDIVIDUAL_CUSTOMER", id.toString()));
    }

    public void customerShouldBeActive(Customer customer) {
        if (!customer.isActive()) {
            throw new BusinessException("Müşteri aktif değil", BankingErrorCodes.CUSTOMER_INACTIVE);
        }
    }
}
