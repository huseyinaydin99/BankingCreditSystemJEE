package tr.com.huseyinaydin.infrastructure.events;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.ports.IEventPublisher;
import tr.com.huseyinaydin.sharedkernel.events.DomainEvent;

@Component
public class SpringEventPublisher implements IEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public SpringEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(DomainEvent event) {
        this.applicationEventPublisher.publishEvent(event);
    }
}
