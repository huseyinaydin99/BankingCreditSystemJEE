package tr.com.huseyinaydin.infrastructure.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.ports.IEmailNotificationService;
import tr.com.huseyinaydin.application.ports.IUnitOfWork;
import tr.com.huseyinaydin.domain.creditapplication.CreditApplication;
import tr.com.huseyinaydin.domain.customer.Customer;
import tr.com.huseyinaydin.domain.enums.CreditApplicationStatus;
import tr.com.huseyinaydin.domain.events.CorporateCustomerCreatedEvent;
import tr.com.huseyinaydin.domain.events.CreditApplicationApprovedEvent;
import tr.com.huseyinaydin.domain.events.CreditApplicationRejectedEvent;
import tr.com.huseyinaydin.domain.events.IndividualCustomerCreatedEvent;

import java.util.Optional;

@Component
public class NotificationEventListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventListener.class);
    
    private final IEmailNotificationService emailNotificationService;
    private final IUnitOfWork uow;

    public NotificationEventListener(IEmailNotificationService emailNotificationService, IUnitOfWork uow) {
        this.emailNotificationService = emailNotificationService;
        this.uow = uow;
    }

    @EventListener
    public void handle(CreditApplicationApprovedEvent event) {
        log.info("Received CreditApplicationApprovedEvent for application {}", event.getApplicationId());
        
        Optional<CreditApplication> applicationOpt = uow.creditApplications().findById(event.getApplicationId());
        if (applicationOpt.isPresent()) {
            Customer customer = applicationOpt.get().getCustomer();
            if (customer != null && customer.getEmail() != null) {
                emailNotificationService.sendCreditApplicationStatusChanged(
                        event.getApplicationId(),
                        customer.getEmail(),
                        CreditApplicationStatus.APPROVED,
                        null
                );
            }
        }
    }

    @EventListener
    public void handle(CreditApplicationRejectedEvent event) {
        log.info("Received CreditApplicationRejectedEvent for application {}", event.getApplicationId());
        
        Optional<CreditApplication> applicationOpt = uow.creditApplications().findById(event.getApplicationId());
        if (applicationOpt.isPresent()) {
            Customer customer = applicationOpt.get().getCustomer();
            if (customer != null && customer.getEmail() != null) {
                emailNotificationService.sendCreditApplicationStatusChanged(
                        event.getApplicationId(),
                        customer.getEmail(),
                        CreditApplicationStatus.REJECTED,
                        event.getReason()
                );
            }
        }
    }

    @EventListener
    public void handle(IndividualCustomerCreatedEvent event) {
        log.info("Received IndividualCustomerCreatedEvent for customer {}", event.getCustomerId());
        if (event.getEmail() != null) {
            String fullName = event.getFirstName() + " " + event.getLastName();
            emailNotificationService.sendWelcomeEmail(event.getEmail(), fullName);
        }
    }

    @EventListener
    public void handle(CorporateCustomerCreatedEvent event) {
        log.info("Received CorporateCustomerCreatedEvent for customer {}", event.getCustomerId());
        if (event.getEmail() != null) {
            emailNotificationService.sendWelcomeEmail(event.getEmail(), event.getCompanyName());
        }
    }
}
