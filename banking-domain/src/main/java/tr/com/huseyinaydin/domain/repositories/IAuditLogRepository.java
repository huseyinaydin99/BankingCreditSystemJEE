package tr.com.huseyinaydin.domain.repositories;

import tr.com.huseyinaydin.domain.audit.AuditLog;
import java.time.Instant;
import java.util.UUID;

public interface IAuditLogRepository extends IAsyncRepository<AuditLog, UUID> {
    int hardDeleteOlderThan(Instant date);
}
