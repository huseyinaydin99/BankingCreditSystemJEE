package tr.com.huseyinaydin.application.creditapplication.rules;

import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.common.BankingErrorCodes;
import tr.com.huseyinaydin.domain.credittype.CreditType;
import tr.com.huseyinaydin.domain.repositories.ICreditTypeRepository;
import tr.com.huseyinaydin.sharedkernel.exception.BusinessException;
import tr.com.huseyinaydin.sharedkernel.exception.NotFoundException;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class CreditApplicationBusinessRules {

    private final ICreditTypeRepository creditTypeRepository;

    public CreditApplicationBusinessRules(ICreditTypeRepository creditTypeRepository) {
        this.creditTypeRepository = creditTypeRepository;
    }

    public CreditType creditTypeMustExist(UUID id) {
        return creditTypeRepository.findById(id).orElseThrow(() ->
                new NotFoundException("CREDIT_TYPE", id.toString()));
    }

    public void amountMustBeInRange(BigDecimal amount, CreditType creditType) {
        if (amount.compareTo(creditType.getMinAmount()) < 0
                || amount.compareTo(creditType.getMaxAmount()) > 0) {
            throw new BusinessException(
                    "Talep edilen tutar kredi türü limitlerinin dışında. Min: "
                            + creditType.getMinAmount() + ", Max: " + creditType.getMaxAmount(),
                    BankingErrorCodes.AMOUNT_OUT_OF_RANGE
            );
        }
    }

    public void termMustBeInRange(int term, CreditType creditType) {
        if (term < creditType.getMinTerm() || term > creditType.getMaxTerm()) {
            throw new BusinessException(
                    "Talep edilen vade kredi türü limitlerinin dışında. Min: "
                            + creditType.getMinTerm() + ", Max: " + creditType.getMaxTerm(),
                    BankingErrorCodes.TERM_OUT_OF_RANGE
            );
        }
    }
}
