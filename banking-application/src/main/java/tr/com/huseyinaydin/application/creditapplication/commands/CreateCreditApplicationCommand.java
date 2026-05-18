package tr.com.huseyinaydin.application.creditapplication.commands;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.creditapplication.rules.CreditApplicationBusinessRules;
import tr.com.huseyinaydin.application.ports.IUnitOfWork;
import tr.com.huseyinaydin.domain.creditapplication.CreditApplication;
import tr.com.huseyinaydin.domain.credittype.CreditType;
import tr.com.huseyinaydin.domain.customer.Customer;
import tr.com.huseyinaydin.sharedkernel.exception.NotFoundException;
import tr.com.huseyinaydin.sharedkernel.messaging.ICommand;
import tr.com.huseyinaydin.sharedkernel.messaging.ICommandHandler;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreateCreditApplicationCommand(
        @NotNull UUID customerId,
        @NotNull UUID creditTypeId,
        @NotNull @Positive @DecimalMax("10000000") BigDecimal requestedAmount,
        @Min(1) @Max(360) int requestedTerm
) implements ICommand<CreateCreditApplicationCommand.Response> {

    public record Response(
            UUID id,
            UUID customerId,
            UUID creditTypeId,
            BigDecimal requestedAmount,
            int requestedTerm,
            BigDecimal estimatedMonthlyPayment,
            BigDecimal estimatedTotalPayment,
            String status,
            LocalDateTime createdDate
    ) {}

    @Component
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public static class Handler
            implements ICommandHandler<CreateCreditApplicationCommand, Response> {

        private final IUnitOfWork uow;
        private final CreditApplicationBusinessRules businessRules;

        public Handler(IUnitOfWork uow, CreditApplicationBusinessRules businessRules) {
            this.uow = uow;
            this.businessRules = businessRules;
        }

        @Override
        public Response handle(CreateCreditApplicationCommand command) {
            CreditType creditType = businessRules.creditTypeMustExist(command.creditTypeId());
            businessRules.amountMustBeInRange(command.requestedAmount(), creditType);
            businessRules.termMustBeInRange(command.requestedTerm(), creditType);

            Customer customer = uow.individualCustomers()
                    .findById(command.customerId())
                    .<Customer>map(c -> c)
                    .orElseGet(() -> uow.corporateCustomers()
                            .findById(command.customerId())
                            .<Customer>map(c -> c)
                            .orElse(null));

            if (customer == null) {
                throw new NotFoundException("CUSTOMER", command.customerId().toString());
            }

            CreditApplication application = new CreditApplication(
                    customer,
                    command.creditTypeId(),
                    command.requestedAmount(),
                    command.requestedTerm()
            );

            application.calculatePayments(
                    command.requestedAmount(),
                    command.requestedTerm(),
                    creditType.getBaseInterestRate()
            );

            uow.beginTransaction();
            uow.creditApplications().save(application);
            uow.commit();

            return new Response(
                    application.getId(),
                    customer.getId(),
                    command.creditTypeId(),
                    application.getRequestedAmount(),
                    application.getRequestedTerm(),
                    application.getMonthlyPayment(),
                    application.getTotalPayment(),
                    application.getStatus().name(),
                    application.getCreatedDate()
            );
        }
    }
}
