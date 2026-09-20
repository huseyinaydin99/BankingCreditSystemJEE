package tr.com.huseyinaydin.infrastructure.jobs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.cqrs.Mediator;
import tr.com.huseyinaydin.application.creditapplication.commands.SendPendingCreditApplicationRemindersCommand;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
public class PendingCreditApplicationReminderJob {

    private static final Logger log = LoggerFactory.getLogger(PendingCreditApplicationReminderJob.class);

    private final Mediator mediator;

    public PendingCreditApplicationReminderJob(Mediator mediator) {
        this.mediator = mediator;
    }

    @Scheduled(cron = "${banking.jobs.pending-reminder.cron}")
    public void execute() {
        log.info("Starting PendingCreditApplicationReminderJob...");
        Instant threeDaysAgo = Instant.now().minus(3, ChronoUnit.DAYS);

        try {
            Integer count = mediator.send(new SendPendingCreditApplicationRemindersCommand(threeDaysAgo));
            log.info("Finished PendingCreditApplicationReminderJob. Processed {} applications.", count);
        } catch (Exception e) {
            log.error("Error occurred in PendingCreditApplicationReminderJob", e);
        }
    }
}
