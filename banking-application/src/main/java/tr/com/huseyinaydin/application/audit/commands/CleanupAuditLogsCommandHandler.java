package tr.com.huseyinaydin.application.audit.commands;

import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.sharedkernel.messaging.ICommandHandler;
import tr.com.huseyinaydin.domain.repositories.IAuditLogRepository;

@Component
public class CleanupAuditLogsCommandHandler implements ICommandHandler<CleanupAuditLogsCommand, Integer> {

    private final IAuditLogRepository auditLogRepository;

    public CleanupAuditLogsCommandHandler(IAuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public Integer handle(CleanupAuditLogsCommand request) {
        return auditLogRepository.hardDeleteOlderThan(request.getOlderThan());
    }
}
