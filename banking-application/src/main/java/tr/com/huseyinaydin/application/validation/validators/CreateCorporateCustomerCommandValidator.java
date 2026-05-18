package tr.com.huseyinaydin.application.validation.validators;

import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.customers.commands.CreateCorporateCustomerCommand;
import tr.com.huseyinaydin.application.validation.AbstractValidator;
import tr.com.huseyinaydin.application.validation.BankingValidationCodes;

import java.util.regex.Pattern;

@Component
public class CreateCorporateCustomerCommandValidator
        extends AbstractValidator<CreateCorporateCustomerCommand> {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\+90[5][0-9]{9}$");

    public CreateCorporateCustomerCommandValidator() {

        addRule(r -> isBlank(r.companyName()),
                "companyName", BankingValidationCodes.COMPANY_NAME_REQUIRED);
        addRule(r -> !isBlank(r.companyName()) && r.companyName().length() > 100,
                "companyName", BankingValidationCodes.COMPANY_NAME_MAX_LENGTH, r -> r.companyName());

        addRule(r -> isBlank(r.taxNumber()),
                "taxNumber", BankingValidationCodes.TAX_NUMBER_REQUIRED);
        addRule(r -> !isBlank(r.taxNumber()) && !isValidVkn(r.taxNumber()),
                "taxNumber", BankingValidationCodes.TAX_NUMBER_INVALID, r -> r.taxNumber());

        addRule(r -> isBlank(r.authorizedPersonName()),
                "authorizedPersonName", BankingValidationCodes.AUTHORIZED_PERSON_REQUIRED);

        addRule(r -> isBlank(r.email()),
                "email", BankingValidationCodes.EMAIL_REQUIRED);
        addRule(r -> !isBlank(r.email()) && !EMAIL_PATTERN.matcher(r.email()).matches(),
                "email", BankingValidationCodes.EMAIL_INVALID, r -> r.email());

        addRule(r -> isBlank(r.phoneNumber()),
                "phoneNumber", BankingValidationCodes.PHONE_REQUIRED);
        addRule(r -> !isBlank(r.phoneNumber()) && !PHONE_PATTERN.matcher(r.phoneNumber()).matches(),
                "phoneNumber", BankingValidationCodes.PHONE_INVALID, r -> r.phoneNumber());

        addRule(r -> isBlank(r.password()),
                "password", BankingValidationCodes.PASSWORD_REQUIRED);
        addRule(r -> !isBlank(r.password()) && r.password().length() < 8,
                "password", BankingValidationCodes.PASSWORD_TOO_SHORT);
        addRule(r -> !isBlank(r.password()) && r.password().chars().noneMatch(Character::isUpperCase),
                "password", BankingValidationCodes.PASSWORD_NEEDS_UPPERCASE);
        addRule(r -> !isBlank(r.password()) && r.password().chars().noneMatch(Character::isLowerCase),
                "password", BankingValidationCodes.PASSWORD_NEEDS_LOWERCASE);
        addRule(r -> !isBlank(r.password()) && r.password().chars().noneMatch(Character::isDigit),
                "password", BankingValidationCodes.PASSWORD_NEEDS_DIGIT);
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static boolean isValidVkn(String value) {
        if (!value.matches("\\d{10}")) return false;
        int[] d = new int[10];
        for (int i = 0; i < 10; i++) d[i] = value.charAt(i) - '0';
        int checksum = 0;
        for (int i = 0; i < 9; i++) {
            int p = (d[i] + (9 - i)) % 10;
            if (p != 0) {
                p = (p * (int) Math.pow(2, 9 - i)) % 9;
                if (p == 0) p = 9;
            }
            checksum += p;
        }
        return d[9] == checksum % 10;
    }
}
