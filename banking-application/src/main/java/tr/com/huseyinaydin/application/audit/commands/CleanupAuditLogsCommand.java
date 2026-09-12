package tr.com.huseyinaydin.application.audit.commands;

import tr.com.huseyinaydin.sharedkernel.messaging.ICommand;

import java.time.Instant;

public class CleanupAuditLogsCommand implements ICommand<Integer> {
    private final Instant olderThan;

    public CleanupAuditLogsCommand(Instant olderThan) {
        this.olderThan = olderThan;
    }

    public Instant getOlderThan() { return olderThan; }
}
