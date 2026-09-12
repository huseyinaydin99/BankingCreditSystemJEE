package tr.com.huseyinaydin.infrastructure.jobs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.ports.IEmailNotificationService;
import tr.com.huseyinaydin.domain.creditapplication.CreditApplication;
import tr.com.huseyinaydin.domain.enums.CreditApplicationStatus;
import tr.com.huseyinaydin.domain.repositories.ICreditApplicationRepository;
import tr.com.huseyinaydin.domain.repositories.IApplicationUserRepository;
import tr.com.huseyinaydin.domain.repositories.Specification;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class PendingCreditApplicationReminderJob {

    private static final Logger log = LoggerFactory.getLogger(PendingCreditApplicationReminderJob.class);

    private final ICreditApplicationRepository creditApplicationRepository;
    private final IApplicationUserRepository applicationUserRepository;
    private final IEmailNotificationService emailNotificationService;

    public PendingCreditApplicationReminderJob(ICreditApplicationRepository creditApplicationRepository,
                                               IApplicationUserRepository applicationUserRepository,
                                               IEmailNotificationService emailNotificationService) {
        this.creditApplicationRepository = creditApplicationRepository;
        this.applicationUserRepository = applicationUserRepository;
        this.emailNotificationService = emailNotificationService;
    }

    @Scheduled(cron = "${banking.jobs.pending-reminder.cron}")
    public void execute() {
        log.info("Starting PendingCreditApplicationReminderJob...");
        Instant threeDaysAgo = Instant.now().minus(3, ChronoUnit.DAYS);

        Specification<CreditApplication> spec = (root, query, cb) -> cb.and(
                cb.equal(root.get("status"), CreditApplicationStatus.PENDING),
                cb.lessThan(root.get("createdDate"), threeDaysAgo)
        );

        List<CreditApplication> pendingApps = creditApplicationRepository.findAll(
                spec, new tr.com.huseyinaydin.sharedkernel.pagination.PaginationRequest(0, 1000)).getItems();

        for (CreditApplication app : pendingApps) {
            
            Specification<tr.com.huseyinaydin.domain.user.ApplicationUser> userSpec = (root, query, cb) -> cb.equal(root.get("customerId"), app.getCustomerId());
            applicationUserRepository.findAll(userSpec, new tr.com.huseyinaydin.sharedkernel.pagination.PaginationRequest(0, 1)).getItems().stream().findFirst().ifPresent(user -> {
                try {
                    emailNotificationService.sendCreditApplicationStatusChanged(
                            app.getId(), user.getEmail(), app.getStatus(), "Hatirlatma: Basvurunuz onay bekliyor.");
                } catch (Exception e) {
                    log.error("Failed to send reminder for application {}", app.getId(), e);
                }
            });

        }
        log.info("Finished PendingCreditApplicationReminderJob. Processed {} applications.", pendingApps.size());
    }
}
