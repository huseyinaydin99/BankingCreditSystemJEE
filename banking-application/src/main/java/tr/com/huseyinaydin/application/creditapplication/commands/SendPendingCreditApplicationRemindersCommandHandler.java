package tr.com.huseyinaydin.application.creditapplication.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.ports.IEmailNotificationService;
import tr.com.huseyinaydin.application.ports.read.ICreditApplicationReadService;
import tr.com.huseyinaydin.application.creditapplication.dtos.PendingApplicationReminderDto;
import tr.com.huseyinaydin.domain.enums.CreditApplicationStatus;
import tr.com.huseyinaydin.sharedkernel.messaging.ICommandHandler;

import java.util.List;

@Component
public class SendPendingCreditApplicationRemindersCommandHandler implements ICommandHandler<SendPendingCreditApplicationRemindersCommand, Integer> {

    private static final Logger log = LoggerFactory.getLogger(SendPendingCreditApplicationRemindersCommandHandler.class);

    private final ICreditApplicationReadService readService;
    private final IEmailNotificationService emailNotificationService;

    public SendPendingCreditApplicationRemindersCommandHandler(ICreditApplicationReadService readService, IEmailNotificationService emailNotificationService) {
        this.readService = readService;
        this.emailNotificationService = emailNotificationService;
    }

    @Override
    public Integer handle(SendPendingCreditApplicationRemindersCommand request) {
        
        List<PendingApplicationReminderDto> reminders = readService.getPendingApplicationsOlderThan(request.getOlderThan());

        int count = 0;
        for (PendingApplicationReminderDto dto : reminders) {
            try {
                emailNotificationService.sendCreditApplicationStatusChanged(
                        dto.applicationId(), dto.customerEmail(), CreditApplicationStatus.PENDING, "Hatirlatma: Basvurunuz onay bekliyor.");
                count++;
            } catch (Exception e) {
                log.error("Failed to send reminder for application {}", dto.applicationId(), e);
            }
        }
        
        return count;
    }
}
