package tr.com.huseyinaydin.sharedkernel.audit;

import java.time.Instant;
import java.util.UUID;


public record AuditEntry(
        String entityId,
        String entityType,
        AuditAction action,
        UUID performedBy,
        Instant performedAt,
        String ipAddress,
        String oldValue,
        String newValue
) {}
