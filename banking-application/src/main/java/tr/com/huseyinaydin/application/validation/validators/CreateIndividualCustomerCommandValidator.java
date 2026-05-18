package tr.com.huseyinaydin.application.validation.validators;

import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.customers.commands.CreateIndividualCustomerCommand;
import tr.com.huseyinaydin.application.validation.AbstractValidator;
import tr.com.huseyinaydin.application.validation.BankingValidationCodes;

import java.time.LocalDate;
import java.util.regex.Pattern;

@Component
public class CreateIndividualCustomerCommandValidator
        extends AbstractValidator<CreateIndividualCustomerCommand> {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\+90[5][0-9]{9}$");

    public CreateIndividualCustomerCommandValidator() {

        addRule(r -> isBlank(r.firstName()),
                "firstName", BankingValidationCodes.FIRST_NAME_REQUIRED);
        addRule(r -> !isBlank(r.firstName()) && r.firstName().length() > 50,
                "firstName", BankingValidationCodes.FIRST_NAME_MAX_LENGTH, r -> r.firstName());

        addRule(r -> isBlank(r.lastName()),
                "lastName", BankingValidationCodes.LAST_NAME_REQUIRED);
        addRule(r -> !isBlank(r.lastName()) && r.lastName().length() > 50,
                "lastName", BankingValidationCodes.LAST_NAME_MAX_LENGTH, r -> r.lastName());

        addRule(r -> isBlank(r.nationalId()),
                "nationalId", BankingValidationCodes.NATIONAL_ID_REQUIRED);
        addRule(r -> !isBlank(r.nationalId()) && !isValidTckn(r.nationalId()),
                "nationalId", BankingValidationCodes.NATIONAL_ID_INVALID, r -> r.nationalId());

        addRule(r -> r.dateOfBirth() == null,
                "dateOfBirth", BankingValidationCodes.DATE_OF_BIRTH_REQUIRED);
        addRule(r -> r.dateOfBirth() != null
                        && r.dateOfBirth().isAfter(LocalDate.now().minusYears(18)),
                "dateOfBirth", BankingValidationCodes.MINIMUM_AGE_REQUIRED, r -> r.dateOfBirth());

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

    private static boolean isValidTckn(String value) {
        if (!value.matches("\\d{11}") || value.charAt(0) == '0') return false;
        int[] d = new int[11];
        for (int i = 0; i < 11; i++) d[i] = value.charAt(i) - '0';
        int oddSum  = d[0] + d[2] + d[4] + d[6] + d[8];
        int evenSum = d[1] + d[3] + d[5] + d[7];
        int d10 = (oddSum * 7 - evenSum) % 10;
        if (d10 < 0) d10 += 10;
        if (d10 != d[9]) return false;
        int total = 0;
        for (int i = 0; i < 10; i++) total += d[i];
        return total % 10 == d[10];
    }
}
