package tr.com.huseyinaydin.application.creditapplication.commands;

import tr.com.huseyinaydin.sharedkernel.messaging.ICommand;
import java.time.Instant;

public class SendPendingCreditApplicationRemindersCommand implements ICommand<Integer> {
    private final Instant olderThan;

    public SendPendingCreditApplicationRemindersCommand(Instant olderThan) {
        this.olderThan = olderThan;
    }

    public Instant getOlderThan() {
        return olderThan;
    }
}
