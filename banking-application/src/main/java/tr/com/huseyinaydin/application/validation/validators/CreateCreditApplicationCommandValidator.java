package tr.com.huseyinaydin.application.validation.validators;

import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.creditapplication.commands.CreateCreditApplicationCommand;
import tr.com.huseyinaydin.application.validation.AbstractValidator;
import tr.com.huseyinaydin.application.validation.BankingValidationCodes;

import java.math.BigDecimal;

@Component
public class CreateCreditApplicationCommandValidator
        extends AbstractValidator<CreateCreditApplicationCommand> {

    private static final BigDecimal MAX_AMOUNT = new BigDecimal("10000000");

    public CreateCreditApplicationCommandValidator() {

        addRule(r -> r.customerId() == null,
                "customerId", BankingValidationCodes.CUSTOMER_ID_REQUIRED);

        addRule(r -> r.creditTypeId() == null,
                "creditTypeId", BankingValidationCodes.CREDIT_TYPE_ID_REQUIRED);

        addRule(r -> r.requestedAmount() == null,
                "requestedAmount", BankingValidationCodes.AMOUNT_REQUIRED);
        addRule(r -> r.requestedAmount() != null
                        && r.requestedAmount().compareTo(BigDecimal.ZERO) <= 0,
                "requestedAmount", BankingValidationCodes.AMOUNT_MUST_BE_POSITIVE,
                r -> r.requestedAmount());
        addRule(r -> r.requestedAmount() != null
                        && r.requestedAmount().compareTo(MAX_AMOUNT) > 0,
                "requestedAmount", BankingValidationCodes.AMOUNT_EXCEEDS_MAX,
                r -> r.requestedAmount());

        addRule(r -> r.requestedTerm() < 1 || r.requestedTerm() > 360,
                "requestedTerm", BankingValidationCodes.TERM_OUT_OF_RANGE,
                r -> r.requestedTerm());
    }
}
