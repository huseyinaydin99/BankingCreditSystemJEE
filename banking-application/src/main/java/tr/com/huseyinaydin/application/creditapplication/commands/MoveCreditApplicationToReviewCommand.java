package tr.com.huseyinaydin.application.creditapplication.commands;

import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.creditapplication.rules.CreditApplicationBusinessRules;
import tr.com.huseyinaydin.domain.repositories.ICreditApplicationRepository;
import tr.com.huseyinaydin.domain.creditapplication.CreditApplication;
import tr.com.huseyinaydin.domain.enums.CreditApplicationStatus;
import tr.com.huseyinaydin.sharedkernel.messaging.ICommand;
import tr.com.huseyinaydin.sharedkernel.messaging.ICommandHandler;

import java.util.UUID;


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

        private final ICreditApplicationRepository creditApplications;
        private final CreditApplicationBusinessRules rules;

        public Handler(ICreditApplicationRepository creditApplications, CreditApplicationBusinessRules rules) {
            this.creditApplications = creditApplications;
            this.rules = rules;
        }

        @Override
        public Response handle(MoveCreditApplicationToReviewCommand command) {
            rules.applicationMustExist(command.id());

            CreditApplication application = creditApplications
                    .findById(command.id())
                    .orElseThrow();

            rules.statusTransitionMustBeValid(application.getStatus(),
                    CreditApplicationStatus.UNDER_REVIEW);

            application.moveToReview();

            creditApplications.update(application);

            return new Response(
                    application.getId(),
                    application.getStatus().name());
        }
    }
}
