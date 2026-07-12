package tr.com.huseyinaydin.application.credittype.rules;

import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.common.BankingErrorCodes;
import tr.com.huseyinaydin.domain.valueobjects.Money;
import tr.com.huseyinaydin.sharedkernel.exception.BusinessException;

import java.math.BigDecimal;

/**
 * CreditType için finansal tutarlılık kurallarını uygulayan domain iş kuralı bileşeni.
 *
 * Kurallar, entity kaydedilmeden önce {@code CreateCreditTypeCommand.Handler} içinde
 * çağrılır ve ihlal durumunda {@link BusinessException} fırlatır (HTTP katmanında
 * RFC 7807 "business-rule-violation" problem tipine eşlenir).
 */
@Component
public class CreditTypeBusinessRules {

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
