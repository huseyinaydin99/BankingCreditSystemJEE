package tr.com.huseyinaydin.application.creditapplication.commands;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.creditapplication.rules.CreditApplicationBusinessRules;
import tr.com.huseyinaydin.application.ports.IUnitOfWork;
import tr.com.huseyinaydin.domain.creditapplication.CreditApplication;
import tr.com.huseyinaydin.domain.enums.CreditApplicationStatus;
import tr.com.huseyinaydin.sharedkernel.messaging.ICommand;
import tr.com.huseyinaydin.sharedkernel.messaging.ICommandHandler;

import java.util.UUID;

/**
 * Kredi başvurusunu reddeder (UNDER_REVIEW → REJECTED). {@code id} yol değişkeninden,
 * {@code rejectionReason} istek gövdesinden ({@link Request}) gelir.
 */
public record RejectCreditApplicationCommand(
        @NotNull UUID id,
        @NotBlank @Size(max = 500) String rejectionReason
) implements ICommand<RejectCreditApplicationCommand.Response> {

    /** HTTP istek gövdesi (id yol değişkeninden alınır, gövdede yer almaz). */
    public record Request(
            @NotBlank @Size(max = 500) String rejectionReason
    ) {}

    public record Response(
            UUID id,
            String status,
            String rejectionReason
    ) {}

    @Component
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public static class Handler
            implements ICommandHandler<RejectCreditApplicationCommand, Response> {

        private final IUnitOfWork uow;
        private final CreditApplicationBusinessRules rules;

        public Handler(IUnitOfWork uow, CreditApplicationBusinessRules rules) {
            this.uow = uow;
            this.rules = rules;
        }

        @Override
        public Response handle(RejectCreditApplicationCommand command) {
            rules.applicationMustExist(command.id());

            CreditApplication application = uow.creditApplications()
                    .findById(command.id())
                    .orElseThrow();

            rules.statusTransitionMustBeValid(application.getStatus(),
                    CreditApplicationStatus.REJECTED);

            application.reject(command.rejectionReason());

            uow.beginTransaction();
            uow.creditApplications().update(application);
            uow.commit();

            return new Response(
                    application.getId(),
                    application.getStatus().name(),
                    application.getRejectionReason());
        }
    }
}
