package tr.com.huseyinaydin.application.users.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tr.com.huseyinaydin.domain.events.CorporateCustomerCreatedEvent;
import tr.com.huseyinaydin.domain.events.IndividualCustomerCreatedEvent;
import tr.com.huseyinaydin.domain.repositories.IApplicationUserRepository;
import tr.com.huseyinaydin.domain.user.ApplicationUser;
import tr.com.huseyinaydin.domain.enums.UserRole;

@Component
public class CustomerCreatedEventHandler {

    private static final Logger log = LoggerFactory.getLogger(CustomerCreatedEventHandler.class);
    private final IApplicationUserRepository userRepository;

    public CustomerCreatedEventHandler(IApplicationUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Ayrı bir transaction (REQUIRES_NEW) açarak sadece ApplicationUser (ikinci Aggregate Root) üzerinde işlem yapıyoruz.
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onIndividualCustomerCreated(IndividualCustomerCreatedEvent event) {
        log.info("Bireysel müşteri eklendi eventi yakalandı, ApplicationUser hesabı oluşturuluyor. Müşteri ID: {}", event.getCustomerId());
        
        ApplicationUser user = new ApplicationUser(
                event.getCustomerId(),
                event.getEmail(),
                event.getPasswordHash(),
                event.getPasswordSalt(),
                UserRole.CUSTOMER
        );
        userRepository.save(user);
    }

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onCorporateCustomerCreated(CorporateCustomerCreatedEvent event) {
        log.info("Kurumsal müşteri eklendi eventi yakalandı, ApplicationUser hesabı oluşturuluyor. Müşteri ID: {}", event.getCustomerId());
        
        ApplicationUser user = new ApplicationUser(
                event.getCustomerId(),
                event.getEmail(),
                event.getPasswordHash(),
                event.getPasswordSalt(),
                UserRole.CUSTOMER
        );
        userRepository.save(user);
    }
}
