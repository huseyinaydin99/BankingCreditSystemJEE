package tr.com.huseyinaydin.infrastructure.jobs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.cqrs.Mediator;
import tr.com.huseyinaydin.application.audit.commands.CleanupAuditLogsCommand;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
public class AuditLogCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(AuditLogCleanupJob.class);
    private final Mediator mediator;

    public AuditLogCleanupJob(Mediator mediator) {
        this.mediator = mediator;
    }

    @Scheduled(cron = "${banking.jobs.audit-cleanup.cron}")
    public void execute() {
        log.info("Starting AuditLogCleanupJob...");
        Instant oneYearAgo = Instant.now().minus(365, ChronoUnit.DAYS);
        
        try {
            Integer deletedCount = mediator.send(new CleanupAuditLogsCommand(oneYearAgo));
            log.info("Finished AuditLogCleanupJob. Deleted {} old audit logs.", deletedCount);
        } catch (Exception e) {
            log.error("Error occurred in AuditLogCleanupJob", e);
        }
    }
}
