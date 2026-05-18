package tr.com.huseyinaydin.application.credittype.commands;

import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.ports.IUnitOfWork;
import tr.com.huseyinaydin.domain.credittype.CreditType;
import tr.com.huseyinaydin.domain.enums.CustomerType;
import tr.com.huseyinaydin.sharedkernel.exception.NotFoundException;
import tr.com.huseyinaydin.sharedkernel.messaging.ICommand;
import tr.com.huseyinaydin.sharedkernel.messaging.ICommandHandler;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreateCreditTypeCommand(
        String name,
        String description,
        CustomerType customerType,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        int minTerm,
        int maxTerm,
        BigDecimal baseInterestRate,
        UUID parentCreditTypeId
) implements ICommand<CreateCreditTypeCommand.Response> {

    public record Response(
            UUID id,
            String name,
            String customerType,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            int minTerm,
            int maxTerm,
            BigDecimal baseInterestRate,
            UUID parentCreditTypeId,
            LocalDateTime createdDate
    ) {}

    @Component
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public static class Handler
            implements ICommandHandler<CreateCreditTypeCommand, Response> {

        private final IUnitOfWork uow;

        public Handler(IUnitOfWork uow) {
            this.uow = uow;
        }

        @Override
        public Response handle(CreateCreditTypeCommand command) {
            CreditType creditType = new CreditType(
                    command.name(),
                    command.customerType(),
                    command.minAmount(),
                    command.maxAmount(),
                    command.minTerm(),
                    command.maxTerm(),
                    command.baseInterestRate()
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
                    creditType.getMinAmount(),
                    creditType.getMaxAmount(),
                    creditType.getMinTerm(),
                    creditType.getMaxTerm(),
                    creditType.getBaseInterestRate(),
                    creditType.getParentCreditTypeId(),
                    creditType.getCreatedDate()
            );
        }
    }
}
