package tr.com.huseyinaydin.sharedkernel.logging;

import java.util.Objects;
import java.util.UUID;

/**
 * İstek bazlı izleme kimliği (correlation id) için değişmez değer nesnesi — UUID sarmalayıcı.
 *
 * {@link #asString()} tam UUID'yi (yayılım/propagation ve MDC için) döner; {@link #toString()}
 * ise loglarda okunabilirlik için kısa (ilk 8 hane) formatı döner.
 */
public final class CorrelationId {

    private final UUID value;

    private CorrelationId(UUID value) {
        this.value = Objects.requireNonNull(value, "correlation id null olamaz");
    }

    /** Yeni rastgele bir correlation id üretir. */
    public static CorrelationId newId() {
        return new CorrelationId(UUID.randomUUID());
    }

    /**
     * Verilen metinden bir correlation id üretir. Metin geçerli bir UUID değilse (null/boş/hatalı),
     * yeni rastgele bir id üretilir.
     */
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

    /** Tam UUID metni — MDC ve X-Correlation-ID header'ı için. */
    public String asString() {
        return value.toString();
    }

    /** Kısa gösterim — UUID'nin ilk 8 hanesi. */
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
