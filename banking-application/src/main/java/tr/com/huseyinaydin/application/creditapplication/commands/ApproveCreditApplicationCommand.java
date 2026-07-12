package tr.com.huseyinaydin.application.creditapplication.commands;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.creditapplication.rules.CreditApplicationBusinessRules;
import tr.com.huseyinaydin.application.ports.IUnitOfWork;
import tr.com.huseyinaydin.domain.creditapplication.CreditApplication;
import tr.com.huseyinaydin.domain.enums.CreditApplicationStatus;
import tr.com.huseyinaydin.domain.valueobjects.Money;
import tr.com.huseyinaydin.sharedkernel.messaging.ICommand;
import tr.com.huseyinaydin.sharedkernel.messaging.ICommandHandler;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Kredi başvurusunu onaylar (UNDER_REVIEW → APPROVED). Onaylanan tutar/vade/faiz ile
 * anüite ödeme planı hesaplanır. {@code id} yol değişkeninden, geri kalan alanlar istek
 * gövdesinden ({@link Request}) gelir.
 */
public record ApproveCreditApplicationCommand(
        @NotNull UUID id,
        @NotNull @Positive @DecimalMax("10000000") BigDecimal approvedAmount,
        @NotNull @Min(1) @Max(360) Integer approvedTerm,
        @NotNull @DecimalMin("0.01") @DecimalMax("99.99") BigDecimal interestRate
) implements ICommand<ApproveCreditApplicationCommand.Response> {

    private static final String DEFAULT_CURRENCY = "TRY";

    /** HTTP istek gövdesi (id yol değişkeninden alınır, gövdede yer almaz). */
    public record Request(
            @NotNull @Positive @DecimalMax("10000000") BigDecimal approvedAmount,
            @NotNull @Min(1) @Max(360) Integer approvedTerm,
            @NotNull @DecimalMin("0.01") @DecimalMax("99.99") BigDecimal interestRate
    ) {}

    public record Response(
            UUID id,
            String status,
            BigDecimal approvedAmount,
            Integer approvedTerm,
            BigDecimal interestRate,
            BigDecimal monthlyPayment,
            BigDecimal totalPayment
    ) {}

    @Component
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public static class Handler
            implements ICommandHandler<ApproveCreditApplicationCommand, Response> {

        private final IUnitOfWork uow;
        private final CreditApplicationBusinessRules rules;

        public Handler(IUnitOfWork uow, CreditApplicationBusinessRules rules) {
            this.uow = uow;
            this.rules = rules;
        }

        @Override
        public Response handle(ApproveCreditApplicationCommand command) {
            rules.applicationMustExist(command.id());

            CreditApplication application = uow.creditApplications()
                    .findById(command.id())
                    .orElseThrow();

            rules.statusTransitionMustBeValid(application.getStatus(),
                    CreditApplicationStatus.APPROVED);

            application.approve(
                    Money.of(command.approvedAmount(), DEFAULT_CURRENCY),
                    command.approvedTerm(),
                    command.interestRate());

            uow.beginTransaction();
            uow.creditApplications().update(application);
            uow.commit();

            return new Response(
                    application.getId(),
                    application.getStatus().name(),
                    application.getApprovedAmount().getAmount(),
                    application.getApprovedTerm(),
                    application.getInterestRate(),
                    application.getMonthlyPayment().getAmount(),
                    application.getTotalPayment().getAmount());
        }
    }
}
