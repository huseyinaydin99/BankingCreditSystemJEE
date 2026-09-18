package tr.com.huseyinaydin.application.credittype.commands;

import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.credittype.rules.CreditTypeBusinessRules;
import tr.com.huseyinaydin.domain.repositories.ICreditTypeRepository;
import tr.com.huseyinaydin.domain.credittype.CreditType;
import tr.com.huseyinaydin.sharedkernel.messaging.ICommand;
import tr.com.huseyinaydin.sharedkernel.messaging.ICommandHandler;

import java.util.UUID;


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

        private final ICreditTypeRepository creditTypes;
        private final CreditTypeBusinessRules rules;

        public Handler(ICreditTypeRepository creditTypes, CreditTypeBusinessRules rules) {
            this.creditTypes = creditTypes;
            this.rules = rules;
        }

        @Override
        public Response handle(DeleteCreditTypeCommand command) {
            rules.creditTypeMustExist(command.id());

            CreditType creditType = creditTypes
                    .findById(command.id())
                    .orElseThrow();

            rules.subCreditTypesMustBeEmpty(creditType);

            creditTypes.delete(creditType, command.permanent());

            return new Response(
                    creditType.getId(),
                    true,
                    "Kredi türü başarıyla silindi"
            );
        }
    }
}
