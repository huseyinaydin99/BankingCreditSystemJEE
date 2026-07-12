package tr.com.huseyinaydin.application.credittype.rules;

import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.common.BankingErrorCodes;
import tr.com.huseyinaydin.domain.credittype.CreditType;
import tr.com.huseyinaydin.domain.repositories.ICreditTypeRepository;
import tr.com.huseyinaydin.domain.valueobjects.Money;
import tr.com.huseyinaydin.sharedkernel.exception.BusinessException;
import tr.com.huseyinaydin.sharedkernel.exception.NotFoundException;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * CreditType için finansal tutarlılık ve yaşam döngüsü kurallarını uygulayan
 * domain iş kuralı bileşeni.
 *
 * Kurallar, ilgili command handler'ları içinde çağrılır ve ihlal durumunda
 * {@link BusinessException}/{@link NotFoundException} fırlatır (HTTP katmanında
 * RFC 7807 problem tiplerine eşlenir).
 */
@Component
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class CreditTypeBusinessRules {

    private final ICreditTypeRepository creditTypeRepository;

    public CreditTypeBusinessRules(ICreditTypeRepository creditTypeRepository) {
        this.creditTypeRepository = creditTypeRepository;
    }

    /** İstenen kredi türü var olmalı; yoksa {@link NotFoundException}. */
    public CreditType creditTypeMustExist(UUID id) {
        return creditTypeRepository.findById(id).orElseThrow(() ->
                new NotFoundException("CREDIT_TYPE", id.toString()));
    }

    /** Alt kredi türü bulunan bir tür silinemez. */
    public void subCreditTypesMustBeEmpty(CreditType creditType) {
        if (creditType.getSubCreditTypes() != null && !creditType.getSubCreditTypes().isEmpty()) {
            throw new BusinessException(
                    "Alt kredi türü olan tür silinemez",
                    BankingErrorCodes.CREDIT_TYPE_HAS_SUBTYPES);
        }
    }

    /**
     * Alt/üst tutar, alt/üst vade ve yıllık faiz oranının kendi içinde tutarlı
     * olduğunu topluca doğrular.
     */
    public void validateFinancialConstraints(Money minimumAmount,
                                             Money maximumAmount,
                                             int minimumTermMonths,
                                             int maximumTermMonths,
                                             BigDecimal annualInterestRate) {
        minimumAmountMustBeLessThanMaximumAmount(minimumAmount, maximumAmount);
        minimumTermMustBeLessThanMaximumTerm(minimumTermMonths, maximumTermMonths);
        annualInterestRateMustBePositive(annualInterestRate);
    }

    public void minimumAmountMustBeLessThanMaximumAmount(Money minimumAmount, Money maximumAmount) {
        if (minimumAmount.getAmount().compareTo(maximumAmount.getAmount()) >= 0) {
            throw new BusinessException(
                    "Minimum tutar maksimum tutardan küçük olmalıdır. Min: "
                            + minimumAmount + ", Max: " + maximumAmount,
                    BankingErrorCodes.CREDIT_TYPE_MIN_AMOUNT_EXCEEDS_MAX);
        }
    }

    public void minimumTermMustBeLessThanMaximumTerm(int minimumTermMonths, int maximumTermMonths) {
        if (minimumTermMonths >= maximumTermMonths) {
            throw new BusinessException(
                    "Minimum vade maksimum vadeden küçük olmalıdır. Min: "
                            + minimumTermMonths + ", Max: " + maximumTermMonths,
                    BankingErrorCodes.CREDIT_TYPE_MIN_TERM_EXCEEDS_MAX);
        }
    }

    public void annualInterestRateMustBePositive(BigDecimal annualInterestRate) {
        if (annualInterestRate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(
                    "Yıllık faiz oranı sıfırdan büyük olmalıdır. Değer: " + annualInterestRate,
                    BankingErrorCodes.CREDIT_TYPE_INTEREST_RATE_INVALID);
        }
    }
}
