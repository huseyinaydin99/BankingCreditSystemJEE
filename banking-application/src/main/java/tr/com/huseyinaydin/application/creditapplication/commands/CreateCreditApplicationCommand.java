package tr.com.huseyinaydin.application.creditapplication.commands;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.creditapplication.rules.CreditApplicationBusinessRules;
import tr.com.huseyinaydin.domain.repositories.IIndividualCustomerRepository;
import tr.com.huseyinaydin.domain.repositories.ICorporateCustomerRepository;
import tr.com.huseyinaydin.domain.repositories.ICreditApplicationRepository;
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

    private static final String DEFAULT_CURRENCY = "TRY";

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

        private final IIndividualCustomerRepository individualCustomers;
        private final ICorporateCustomerRepository corporateCustomers;
        private final ICreditApplicationRepository creditApplications;
        private final CreditApplicationBusinessRules businessRules;

        public Handler(IIndividualCustomerRepository individualCustomers, ICorporateCustomerRepository corporateCustomers, ICreditApplicationRepository creditApplications, CreditApplicationBusinessRules businessRules) {
            this.individualCustomers = individualCustomers;
            this.corporateCustomers = corporateCustomers;
            this.creditApplications = creditApplications;
            this.businessRules = businessRules;
        }

        @Override
        public Response handle(CreateCreditApplicationCommand command) {
            CreditType creditType = businessRules.creditTypeMustExist(command.creditTypeId());
            businessRules.amountMustBeInRange(command.requestedAmount(), creditType);
            businessRules.termMustBeInRange(command.requestedTerm(), creditType);

            Customer customer = individualCustomers
                    .findById(command.customerId())
                    .<Customer>map(c -> c)
                    .orElseGet(() -> corporateCustomers
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
                    creditType.getAnnualInterestRate(),
                    DEFAULT_CURRENCY
            );

            creditApplications.save(application);

            return new Response(
                    application.getId(),
                    customer.getId(),
                    command.creditTypeId(),
                    application.getRequestedAmount(),
                    application.getRequestedTerm(),
                    application.getMonthlyPayment().getAmount(),
                    application.getTotalPayment().getAmount(),
                    application.getStatus().name(),
                    application.getCreatedDate()
            );
        }
    }
}
