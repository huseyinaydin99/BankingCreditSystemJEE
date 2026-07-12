package tr.com.huseyinaydin.application.creditapplication.rules;

import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.common.BankingErrorCodes;
import tr.com.huseyinaydin.domain.creditapplication.CreditApplication;
import tr.com.huseyinaydin.domain.credittype.CreditType;
import tr.com.huseyinaydin.domain.enums.CreditApplicationStatus;
import tr.com.huseyinaydin.domain.repositories.ICreditApplicationRepository;
import tr.com.huseyinaydin.domain.repositories.ICreditTypeRepository;
import tr.com.huseyinaydin.sharedkernel.exception.BusinessException;
import tr.com.huseyinaydin.sharedkernel.exception.NotFoundException;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class CreditApplicationBusinessRules {

    private final ICreditTypeRepository creditTypeRepository;
    private final ICreditApplicationRepository creditApplicationRepository;

    /*
     * İzinli durum geçiş grafiği: PENDING → UNDER_REVIEW → APPROVED/REJECTED.
     * CANCELLED, henüz sonuçlanmamış (PENDING/UNDER_REVIEW) başvurulardan erişilebilir.
     * Terminal durumlar (APPROVED/REJECTED/CANCELLED) için giden geçiş yoktur.
     */
    private static final Map<CreditApplicationStatus, Set<CreditApplicationStatus>> ALLOWED_TRANSITIONS =
            new EnumMap<>(CreditApplicationStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(CreditApplicationStatus.PENDING,
                Set.of(CreditApplicationStatus.UNDER_REVIEW, CreditApplicationStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(CreditApplicationStatus.UNDER_REVIEW,
                Set.of(CreditApplicationStatus.APPROVED, CreditApplicationStatus.REJECTED,
                        CreditApplicationStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(CreditApplicationStatus.APPROVED, Set.of());
        ALLOWED_TRANSITIONS.put(CreditApplicationStatus.REJECTED, Set.of());
        ALLOWED_TRANSITIONS.put(CreditApplicationStatus.CANCELLED, Set.of());
    }

    public CreditApplicationBusinessRules(ICreditTypeRepository creditTypeRepository,
                                          ICreditApplicationRepository creditApplicationRepository) {
        this.creditTypeRepository = creditTypeRepository;
        this.creditApplicationRepository = creditApplicationRepository;
    }

    public CreditType creditTypeMustExist(UUID id) {
        return creditTypeRepository.findById(id).orElseThrow(() ->
                new NotFoundException("CREDIT_TYPE", id.toString()));
    }

    public CreditApplication applicationMustExist(UUID id) {
        return creditApplicationRepository.findById(id).orElseThrow(() ->
                new NotFoundException("CREDIT_APPLICATION", id.toString()));
    }

    /**
     * Onay iş akışının durum geçişini doğrular. Yalnızca
     * PENDING → UNDER_REVIEW → APPROVED/REJECTED (ve iptal) çizgisine izin verilir;
     * geçersiz geçişte {@link BusinessException} fırlatılır.
     */
    public void statusTransitionMustBeValid(CreditApplicationStatus current,
                                            CreditApplicationStatus target) {
        Set<CreditApplicationStatus> allowed =
                ALLOWED_TRANSITIONS.getOrDefault(current, Set.of());
        if (!allowed.contains(target)) {
            throw new BusinessException(
                    "Geçersiz durum geçişi: " + current + " → " + target,
                    BankingErrorCodes.CREDIT_APPLICATION_INVALID_STATUS_TRANSITION);
        }
    }

    public void amountMustBeInRange(BigDecimal amount, CreditType creditType) {
        if (amount.compareTo(creditType.getMinimumAmount().getAmount()) < 0
                || amount.compareTo(creditType.getMaximumAmount().getAmount()) > 0) {
            throw new BusinessException(
                    "Talep edilen tutar kredi türü limitlerinin dışında. Min: "
                            + creditType.getMinimumAmount().getAmount()
                            + ", Max: " + creditType.getMaximumAmount().getAmount(),
                    BankingErrorCodes.AMOUNT_OUT_OF_RANGE
            );
        }
    }

    public void termMustBeInRange(int term, CreditType creditType) {
        if (term < creditType.getMinimumTermMonths() || term > creditType.getMaximumTermMonths()) {
            throw new BusinessException(
                    "Talep edilen vade kredi türü limitlerinin dışında. Min: "
                            + creditType.getMinimumTermMonths() + ", Max: " + creditType.getMaximumTermMonths(),
                    BankingErrorCodes.TERM_OUT_OF_RANGE
            );
        }
    }
}
