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
import tr.com.huseyinaydin.domain.enums.CustomerType;
import tr.com.huseyinaydin.domain.valueobjects.Money;
import tr.com.huseyinaydin.sharedkernel.exception.NotFoundException;
import tr.com.huseyinaydin.sharedkernel.messaging.ICommand;
import tr.com.huseyinaydin.sharedkernel.messaging.ICommandHandler;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreateCreditTypeCommand(
        @NotBlank String name,
        String description,
        @NotNull CustomerType customerType,
        @NotNull @Positive BigDecimal minimumAmount,
        @NotNull @Positive BigDecimal maximumAmount,
        @Min(1) int minimumTermMonths,
        @Min(1) int maximumTermMonths,
        @NotNull @DecimalMin("0.01") @DecimalMax("99.99") BigDecimal annualInterestRate,
        UUID parentCreditTypeId
) implements ICommand<CreateCreditTypeCommand.Response> {

    // Money'nin para birimi API sözleşmesine dahil değildir; sistem geneli varsayılan.
    private static final String DEFAULT_CURRENCY = "TRY";

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
            LocalDateTime createdDate
    ) {}

    @Component
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public static class Handler
            implements ICommandHandler<CreateCreditTypeCommand, Response> {

        private final IUnitOfWork uow;
        private final CreditTypeBusinessRules rules;

        public Handler(IUnitOfWork uow, CreditTypeBusinessRules rules) {
            this.uow = uow;
            this.rules = rules;
        }

        @Override
        public Response handle(CreateCreditTypeCommand command) {
            Money minimumAmount = Money.of(command.minimumAmount(), DEFAULT_CURRENCY);
            Money maximumAmount = Money.of(command.maximumAmount(), DEFAULT_CURRENCY);

            // Domain iş kuralları: tutar/vade/faiz tutarlılığı kaydetmeden önce garanti edilir.
            rules.validateFinancialConstraints(
                    minimumAmount, maximumAmount,
                    command.minimumTermMonths(), command.maximumTermMonths(),
                    command.annualInterestRate());

            CreditType creditType = new CreditType(
                    command.name(),
                    command.customerType(),
                    minimumAmount,
                    maximumAmount,
                    command.minimumTermMonths(),
                    command.maximumTermMonths(),
                    command.annualInterestRate()
            );
            creditType.setDescription(command.description());

            if (command.parentCreditTypeId() != null) {
                CreditType parent = uow.creditTypes()
                        .findById(command.parentCreditTypeId())
                        .orElseThrow(() -> new NotFoundException(
                                "CREDIT_TYPE", command.parentCreditTypeId().toString()));
                creditType.setParentCreditType(parent);
            }

            uow.beginTransaction();
            uow.creditTypes().save(creditType);
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
                    creditType.getCreatedDate()
            );
        }
    }
}
