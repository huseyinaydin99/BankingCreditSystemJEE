package tr.com.huseyinaydin.application.creditapplication.commands;

import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.creditapplication.rules.CreditApplicationBusinessRules;
import tr.com.huseyinaydin.domain.repositories.ICreditApplicationRepository;
import tr.com.huseyinaydin.domain.creditapplication.CreditApplication;
import tr.com.huseyinaydin.sharedkernel.messaging.ICommand;
import tr.com.huseyinaydin.sharedkernel.messaging.ICommandHandler;

import java.util.UUID;


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

        private final ICreditApplicationRepository creditApplications;
        private final CreditApplicationBusinessRules rules;

        public Handler(ICreditApplicationRepository creditApplications, CreditApplicationBusinessRules rules) {
            this.creditApplications = creditApplications;
            this.rules = rules;
        }

        @Override
        public Response handle(DeleteCreditApplicationCommand command) {
            rules.applicationMustExist(command.id());

            CreditApplication application = creditApplications
                    .findById(command.id())
                    .orElseThrow();

            rules.userCanAccessApplication(application);
            rules.onlyPendingCanBeModified(application);

            creditApplications.delete(application, command.permanent());

            return new Response(
                    application.getId(),
                    true,
                    "Kredi başvurusu başarıyla silindi");
        }
    }
}
