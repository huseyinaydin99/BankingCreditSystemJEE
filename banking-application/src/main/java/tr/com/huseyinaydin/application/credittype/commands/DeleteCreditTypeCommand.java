package tr.com.huseyinaydin.application.credittype.commands;

import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.credittype.rules.CreditTypeBusinessRules;
import tr.com.huseyinaydin.application.ports.IUnitOfWork;
import tr.com.huseyinaydin.domain.credittype.CreditType;
import tr.com.huseyinaydin.sharedkernel.messaging.ICommand;
import tr.com.huseyinaydin.sharedkernel.messaging.ICommandHandler;

import java.util.UUID;

/**
 * Bir kredi türünü siler. Alt kredi türü bulunan tür silinemez (BusinessException);
 * aksi hâlde soft delete uygulanır ({@code deletedDate} işaretlenir).
 * Mimari için {@code DeleteIndividualCustomerCommand} örnek alınmıştır.
 */
public record DeleteCreditTypeCommand(
        @NotNull UUID id,
        boolean permanent
) implements ICommand<DeleteCreditTypeCommand.Response> {

    public DeleteCreditTypeCommand(UUID id) {
        this(id, false);
    }

    public record Response(
            UUID id,
            boolean deleted,
            String message
    ) {}

    @Component
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public static class Handler
            implements ICommandHandler<DeleteCreditTypeCommand, Response> {

        private final IUnitOfWork uow;
        private final CreditTypeBusinessRules rules;

        public Handler(IUnitOfWork uow, CreditTypeBusinessRules rules) {
            this.uow = uow;
            this.rules = rules;
        }

        @Override
        public Response handle(DeleteCreditTypeCommand command) {
            rules.creditTypeMustExist(command.id());

            CreditType creditType = uow.creditTypes()
                    .findById(command.id())
                    .orElseThrow();

            rules.subCreditTypesMustBeEmpty(creditType);

            uow.beginTransaction();
            uow.creditTypes().delete(creditType, command.permanent());
            uow.commit();

            return new Response(
                    creditType.getId(),
                    true,
                    "Kredi türü başarıyla silindi"
            );
        }
    }
}
