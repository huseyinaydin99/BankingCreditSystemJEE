package tr.com.huseyinaydin.application.creditapplication.commands;

import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.creditapplication.rules.CreditApplicationBusinessRules;
import tr.com.huseyinaydin.application.ports.IUnitOfWork;
import tr.com.huseyinaydin.domain.creditapplication.CreditApplication;
import tr.com.huseyinaydin.domain.enums.CreditApplicationStatus;
import tr.com.huseyinaydin.sharedkernel.messaging.ICommand;
import tr.com.huseyinaydin.sharedkernel.messaging.ICommandHandler;

import java.util.UUID;

/**
 * Kredi başvurusunu incelemeye alır (PENDING → UNDER_REVIEW). Onay/ret adımları yalnızca
 * UNDER_REVIEW durumundan yapılabildiği için iş akışının ilk adımıdır.
 */
public record MoveCreditApplicationToReviewCommand(
        @NotNull UUID id
) implements ICommand<MoveCreditApplicationToReviewCommand.Response> {

    public record Response(
            UUID id,
            String status
    ) {}

    @Component
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public static class Handler
            implements ICommandHandler<MoveCreditApplicationToReviewCommand, Response> {

        private final IUnitOfWork uow;
        private final CreditApplicationBusinessRules rules;

        public Handler(IUnitOfWork uow, CreditApplicationBusinessRules rules) {
            this.uow = uow;
            this.rules = rules;
        }

        @Override
        public Response handle(MoveCreditApplicationToReviewCommand command) {
            rules.applicationMustExist(command.id());

            CreditApplication application = uow.creditApplications()
                    .findById(command.id())
                    .orElseThrow();

            rules.statusTransitionMustBeValid(application.getStatus(),
                    CreditApplicationStatus.UNDER_REVIEW);

            application.moveToReview();

            uow.beginTransaction();
            uow.creditApplications().update(application);
            uow.commit();

            return new Response(
                    application.getId(),
                    application.getStatus().name());
        }
    }
}
