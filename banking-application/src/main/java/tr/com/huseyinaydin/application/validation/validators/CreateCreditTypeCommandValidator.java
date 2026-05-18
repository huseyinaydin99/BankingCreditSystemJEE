package tr.com.huseyinaydin.application.validation.validators;

import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.credittype.commands.CreateCreditTypeCommand;
import tr.com.huseyinaydin.application.validation.AbstractValidator;
import tr.com.huseyinaydin.application.validation.BankingValidationCodes;

import java.math.BigDecimal;

@Component
public class CreateCreditTypeCommandValidator
        extends AbstractValidator<CreateCreditTypeCommand> {

    private static final BigDecimal MIN_RATE = new BigDecimal("0.01");
    private static final BigDecimal MAX_RATE = new BigDecimal("99.99");

    public CreateCreditTypeCommandValidator() {

        addRule(r -> r.name() == null || r.name().isBlank(),
                "name", BankingValidationCodes.CREDIT_TYPE_NAME_REQUIRED);

        addRule(r -> r.customerType() == null,
                "customerType", BankingValidationCodes.CUSTOMER_TYPE_REQUIRED);

        addRule(r -> r.minAmount() == null,
                "minAmount", BankingValidationCodes.MIN_AMOUNT_REQUIRED);
        addRule(r -> r.minAmount() != null && r.minAmount().compareTo(BigDecimal.ZERO) <= 0,
                "minAmount", BankingValidationCodes.MIN_AMOUNT_MUST_BE_POSITIVE,
                r -> r.minAmount());

        addRule(r -> r.maxAmount() == null,
                "maxAmount", BankingValidationCodes.MAX_AMOUNT_REQUIRED);
        addRule(r -> r.maxAmount() != null && r.maxAmount().compareTo(BigDecimal.ZERO) <= 0,
                "maxAmount", BankingValidationCodes.MAX_AMOUNT_MUST_BE_POSITIVE,
                r -> r.maxAmount());

        addRule(r -> r.minAmount() != null && r.maxAmount() != null
                        && r.minAmount().compareTo(r.maxAmount()) >= 0,
                "minAmount", BankingValidationCodes.MIN_AMOUNT_EXCEEDS_MAX_AMOUNT,
                r -> r.minAmount());

        addRule(r -> r.minTerm() < 1,
                "minTerm", BankingValidationCodes.MIN_TERM_INVALID, r -> r.minTerm());

        addRule(r -> r.maxTerm() < 1,
                "maxTerm", BankingValidationCodes.MAX_TERM_INVALID, r -> r.maxTerm());

        addRule(r -> r.minTerm() >= 1 && r.maxTerm() >= 1 && r.minTerm() >= r.maxTerm(),
                "minTerm", BankingValidationCodes.MIN_TERM_EXCEEDS_MAX_TERM, r -> r.minTerm());

        addRule(r -> r.baseInterestRate() == null,
                "baseInterestRate", BankingValidationCodes.INTEREST_RATE_REQUIRED);
        addRule(r -> r.baseInterestRate() != null
                        && (r.baseInterestRate().compareTo(MIN_RATE) < 0
                            || r.baseInterestRate().compareTo(MAX_RATE) > 0),
                "baseInterestRate", BankingValidationCodes.INTEREST_RATE_OUT_OF_RANGE,
                r -> r.baseInterestRate());
    }
}
