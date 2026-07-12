package tr.com.huseyinaydin.application.creditapplication.commands;

import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.creditapplication.rules.CreditApplicationBusinessRules;
import tr.com.huseyinaydin.application.ports.IUnitOfWork;
import tr.com.huseyinaydin.domain.creditapplication.CreditApplication;
import tr.com.huseyinaydin.sharedkernel.messaging.ICommand;
import tr.com.huseyinaydin.sharedkernel.messaging.ICommandHandler;

import java.util.UUID;

/**
 * Bir kredi başvurusunu soft delete ile siler. Yalnızca PENDING durumundakiler
 * silinebilir; UNDER_REVIEW/APPROVED (veya diğer) durumlarda {@code BusinessException},
 * yetkisiz erişimde {@code AuthorizationException} fırlatılır.
 * Mimari için {@code DeleteIndividualCustomerCommand} örnek alınmıştır.
 */
public record DeleteCreditApplicationCommand(
        @NotNull UUID id,
        boolean permanent
) implements ICommand<DeleteCreditApplicationCommand.Response> {

    public DeleteCreditApplicationCommand(UUID id) {
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
            implements ICommandHandler<DeleteCreditApplicationCommand, Response> {

        private final IUnitOfWork uow;
        private final CreditApplicationBusinessRules rules;

        public Handler(IUnitOfWork uow, CreditApplicationBusinessRules rules) {
            this.uow = uow;
            this.rules = rules;
        }

        @Override
        public Response handle(DeleteCreditApplicationCommand command) {
            rules.applicationMustExist(command.id());

            CreditApplication application = uow.creditApplications()
                    .findById(command.id())
                    .orElseThrow();

            rules.userCanAccessApplication(application);
            rules.onlyPendingCanBeModified(application);

            uow.beginTransaction();
            uow.creditApplications().delete(application, command.permanent());
            uow.commit();

            return new Response(
                    application.getId(),
                    true,
                    "Kredi başvurusu başarıyla silindi");
        }
    }
}
