package tr.com.huseyinaydin.domain.common;

import tr.com.huseyinaydin.domain.events.DomainEvent;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@MappedSuperclass
public abstract class Entity<TId> extends BaseEntity<TId> {

    @Transient
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    protected Entity() {
        super();
    }

    protected void registerEvent(DomainEvent event) {
        domainEvents.add(event);
    }

    public List<DomainEvent> pullEvents() {
        List<DomainEvent> events = new ArrayList<>(domainEvents);
        domainEvents.clear();
        return Collections.unmodifiableList(events);
    }
}
