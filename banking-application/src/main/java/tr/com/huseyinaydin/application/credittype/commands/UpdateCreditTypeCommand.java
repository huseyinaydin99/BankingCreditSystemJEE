package tr.com.huseyinaydin.application.credittype.commands;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.credittype.rules.CreditTypeBusinessRules;
import tr.com.huseyinaydin.application.ports.IUnitOfWork;
import tr.com.huseyinaydin.domain.credittype.CreditType;
import tr.com.huseyinaydin.domain.valueobjects.Money;
import tr.com.huseyinaydin.sharedkernel.exception.NotFoundException;
import tr.com.huseyinaydin.sharedkernel.messaging.ICommand;
import tr.com.huseyinaydin.sharedkernel.messaging.ICommandHandler;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Var olan bir kredi türünü günceller. {@code id} yol değişkeninden, geri kalan alanlar
 * istek gövdesinden ({@link Request}) gelir. {@code customerType} güncellenmez (Create'te
 * belirlenir). Mimari için {@link CreateCreditTypeCommand} örnek alınmıştır.
 */
public record UpdateCreditTypeCommand(
        @NotNull UUID id,
        @NotBlank String name,
        String description,
        @NotNull @Positive BigDecimal minimumAmount,
        @NotNull @Positive BigDecimal maximumAmount,
        @Min(1) int minimumTermMonths,
        @Min(1) int maximumTermMonths,
        @NotNull @DecimalMin("0.01") @DecimalMax("99.99") BigDecimal annualInterestRate,
        UUID parentCreditTypeId
) implements ICommand<UpdateCreditTypeCommand.Response> {

    private static final String DEFAULT_CURRENCY = "TRY";

    /** HTTP istek gövdesi (id yol değişkeninden alınır, gövdede yer almaz). */
    public record Request(
            @NotBlank String name,
            String description,
            @NotNull @Positive BigDecimal minimumAmount,
            @NotNull @Positive BigDecimal maximumAmount,
            @Min(1) int minimumTermMonths,
            @Min(1) int maximumTermMonths,
            @NotNull @DecimalMin("0.01") @DecimalMax("99.99") BigDecimal annualInterestRate,
            UUID parentCreditTypeId
    ) {}

    public record Response(
            UUID id,
            String name,
            String customerType,
            BigDecimal minimumAmount,
            BigDecimal maximumAmount,
            int minimumTermMonths,
            int maximumTermMonths,
            BigDecimal annualInterestRate,
            UUID parentCreditTypeId,
            LocalDateTime updatedDate
    ) {}

    @Component
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public static class Handler
            implements ICommandHandler<UpdateCreditTypeCommand, Response> {

        private final IUnitOfWork uow;
        private final CreditTypeBusinessRules rules;

        public Handler(IUnitOfWork uow, CreditTypeBusinessRules rules) {
            this.uow = uow;
            this.rules = rules;
        }

        @Override
        public Response handle(UpdateCreditTypeCommand command) {
            rules.creditTypeMustExist(command.id());

            CreditType creditType = uow.creditTypes()
                    .findById(command.id())
                    .orElseThrow();

            Money minimumAmount = Money.of(command.minimumAmount(), DEFAULT_CURRENCY);
            Money maximumAmount = Money.of(command.maximumAmount(), DEFAULT_CURRENCY);

            rules.validateFinancialConstraints(
                    minimumAmount, maximumAmount,
                    command.minimumTermMonths(), command.maximumTermMonths(),
                    command.annualInterestRate());

            creditType.setName(command.name());
            creditType.setDescription(command.description());
            creditType.setMinimumAmount(minimumAmount);
            creditType.setMaximumAmount(maximumAmount);
            creditType.setMinimumTermMonths(command.minimumTermMonths());
            creditType.setMaximumTermMonths(command.maximumTermMonths());
            creditType.setAnnualInterestRate(command.annualInterestRate());

            if (command.parentCreditTypeId() != null) {
                CreditType parent = uow.creditTypes()
                        .findById(command.parentCreditTypeId())
                        .orElseThrow(() -> new NotFoundException(
                                "CREDIT_TYPE", command.parentCreditTypeId().toString()));
                creditType.setParentCreditType(parent);
            } else {
                creditType.setParentCreditType(null);
            }

            uow.beginTransaction();
            uow.creditTypes().update(creditType);
            uow.commit();

            return new Response(
                    creditType.getId(),
                    creditType.getName(),
                    creditType.getCustomerType().name(),
                    creditType.getMinimumAmount().getAmount(),
                    creditType.getMaximumAmount().getAmount(),
                    creditType.getMinimumTermMonths(),
                    creditType.getMaximumTermMonths(),
                    creditType.getAnnualInterestRate(),
                    creditType.getParentCreditTypeId(),
                    creditType.getUpdatedDate()
            );
        }
    }
}
