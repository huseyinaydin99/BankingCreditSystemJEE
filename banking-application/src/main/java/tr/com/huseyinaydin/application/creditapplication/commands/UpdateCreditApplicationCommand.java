package tr.com.huseyinaydin.application.creditapplication.commands;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.creditapplication.rules.CreditApplicationBusinessRules;
import tr.com.huseyinaydin.domain.repositories.ICreditApplicationRepository;
import tr.com.huseyinaydin.domain.creditapplication.CreditApplication;
import tr.com.huseyinaydin.domain.credittype.CreditType;
import tr.com.huseyinaydin.sharedkernel.messaging.ICommand;
import tr.com.huseyinaydin.sharedkernel.messaging.ICommandHandler;

import java.math.BigDecimal;
import java.util.UUID;


public record UpdateCreditApplicationCommand(
        @NotNull UUID id,
        @NotNull UUID creditTypeId,
        @NotNull @Positive @DecimalMax("10000000") BigDecimal requestedAmount,
        @Min(1) @Max(360) int requestedTerm
) implements ICommand<UpdateCreditApplicationCommand.Response> {

    private static final String DEFAULT_CURRENCY = "TRY";

    public record Request(
            @NotNull UUID creditTypeId,
            @NotNull @Positive @DecimalMax("10000000") BigDecimal requestedAmount,
            @Min(1) @Max(360) int requestedTerm
    ) {}

    public record Response(
            UUID id,
            UUID customerId,
            UUID creditTypeId,
            BigDecimal requestedAmount,
            int requestedTerm,
            BigDecimal estimatedMonthlyPayment,
            BigDecimal estimatedTotalPayment,
            String status
    ) {}

    @Component
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public static class Handler
            implements ICommandHandler<UpdateCreditApplicationCommand, Response> {

        private final ICreditApplicationRepository creditApplications;
        private final CreditApplicationBusinessRules rules;

        public Handler(ICreditApplicationRepository creditApplications, CreditApplicationBusinessRules rules) {
            this.creditApplications = creditApplications;
            this.rules = rules;
        }

        @Override
        public Response handle(UpdateCreditApplicationCommand command) {
            rules.applicationMustExist(command.id());

            CreditApplication application = creditApplications
                    .findById(command.id())
                    .orElseThrow();

            rules.userCanAccessApplication(application);
            rules.onlyPendingCanBeModified(application);

            CreditType creditType = rules.creditTypeMustExist(command.creditTypeId());
            rules.amountMustBeInRange(command.requestedAmount(), creditType);
            rules.termMustBeInRange(command.requestedTerm(), creditType);

            application.updateRequest(
                    command.creditTypeId(), command.requestedAmount(), command.requestedTerm());
            application.calculatePayments(
                    command.requestedAmount(), command.requestedTerm(),
                    creditType.getAnnualInterestRate(), DEFAULT_CURRENCY);

            creditApplications.update(application);

            return new Response(
                    application.getId(),
                    application.getCustomerId(),
                    application.getCreditTypeId(),
                    application.getRequestedAmount(),
                    application.getRequestedTerm(),
                    application.getMonthlyPayment().getAmount(),
                    application.getTotalPayment().getAmount(),
                    application.getStatus().name());
        }
    }
}
