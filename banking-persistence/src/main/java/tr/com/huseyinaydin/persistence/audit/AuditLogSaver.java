package tr.com.huseyinaydin.persistence.audit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tr.com.huseyinaydin.domain.audit.AuditLog;
import tr.com.huseyinaydin.domain.repositories.IAuditLogRepository;

@Service
public class AuditLogSaver {

    private final IAuditLogRepository auditLogRepository;

    public AuditLogSaver(IAuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    // Must be REQUIRES_NEW to commit independently of the current JPA transaction flush
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveAuditLog(AuditLog log) {
        auditLogRepository.save(log);
    }
}
