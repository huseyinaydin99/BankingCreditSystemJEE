package tr.com.huseyinaydin.application.creditapplication.commands;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.creditapplication.rules.CreditApplicationBusinessRules;
import tr.com.huseyinaydin.domain.repositories.ICreditApplicationRepository;
import tr.com.huseyinaydin.domain.creditapplication.CreditApplication;
import tr.com.huseyinaydin.domain.enums.CreditApplicationStatus;
import tr.com.huseyinaydin.sharedkernel.messaging.ICommand;
import tr.com.huseyinaydin.sharedkernel.messaging.ICommandHandler;

import java.util.UUID;


public record RejectCreditApplicationCommand(
        @NotNull UUID id,
        @NotBlank @Size(max = 500) String rejectionReason
) implements ICommand<RejectCreditApplicationCommand.Response> {

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

        private final ICreditApplicationRepository creditApplications;
        private final CreditApplicationBusinessRules rules;

        public Handler(ICreditApplicationRepository creditApplications, CreditApplicationBusinessRules rules) {
            this.creditApplications = creditApplications;
            this.rules = rules;
        }

        @Override
        public Response handle(RejectCreditApplicationCommand command) {
            rules.applicationMustExist(command.id());

            CreditApplication application = creditApplications
                    .findById(command.id())
                    .orElseThrow();

            rules.statusTransitionMustBeValid(application.getStatus(),
                    CreditApplicationStatus.REJECTED);

            application.reject(command.rejectionReason());

            creditApplications.update(application);

            return new Response(
                    application.getId(),
                    application.getStatus().name(),
                    application.getRejectionReason());
        }
    }
}
