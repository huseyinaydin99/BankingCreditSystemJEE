package tr.com.huseyinaydin.infrastructure.jobs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.cqrs.Mediator;
import tr.com.huseyinaydin.application.creditapplication.commands.CancelExpiredApplicationsCommand;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
public class ExpiredCreditApplicationJob {

    private static final Logger log = LoggerFactory.getLogger(ExpiredCreditApplicationJob.class);
    private final Mediator mediator;

    public ExpiredCreditApplicationJob(Mediator mediator) {
        this.mediator = mediator;
    }

    @Scheduled(cron = "${banking.jobs.expired.cron}")
    public void execute() {
        log.info("Starting ExpiredCreditApplicationJob...");
        Instant thirtyDaysAgo = Instant.now().minus(30, ChronoUnit.DAYS);
        
        try {
            Integer count = mediator.send(new CancelExpiredApplicationsCommand(thirtyDaysAgo));
            log.info("Finished ExpiredCreditApplicationJob. Cancelled {} expired applications.", count);
        } catch (Exception e) {
            log.error("Error occurred in ExpiredCreditApplicationJob", e);
        }
    }
}
