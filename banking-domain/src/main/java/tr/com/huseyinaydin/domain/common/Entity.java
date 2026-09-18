package tr.com.huseyinaydin.domain.common;

import tr.com.huseyinaydin.sharedkernel.events.DomainEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Entity<TId> extends BaseEntity<TId> {
    
    private transient final List<DomainEvent> domainEvents = new ArrayList<>();

    protected Entity() {
        super();
    }

    protected void addDomainEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }

    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = new ArrayList<>(this.domainEvents);
        this.domainEvents.clear();
        return Collections.unmodifiableList(events);
    }
}
