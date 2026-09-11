package tr.com.huseyinaydin.sharedkernel.logging;

import java.util.Objects;
import java.util.UUID;


public final class CorrelationId {

    private final UUID value;

    private CorrelationId(UUID value) {
        this.value = Objects.requireNonNull(value, "correlation id null olamaz");
    }

    public static CorrelationId newId() {
        return new CorrelationId(UUID.randomUUID());
    }

    public static CorrelationId of(String raw) {
        if (raw == null || raw.isBlank()) {
            return newId();
        }
        try {
            return new CorrelationId(UUID.fromString(raw.trim()));
        } catch (IllegalArgumentException ex) {
            return newId();
        }
    }

    public UUID value() {
        return value;
    }

    public String asString() {
        return value.toString();
    }

    @Override
    public String toString() {
        return value.toString().substring(0, 8);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return value.equals(((CorrelationId) o).value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
