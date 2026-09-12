package tr.com.huseyinaydin.application.ports;

import tr.com.huseyinaydin.sharedkernel.events.DomainEvent;

public interface IEventPublisher {
    void publish(DomainEvent event);
}
