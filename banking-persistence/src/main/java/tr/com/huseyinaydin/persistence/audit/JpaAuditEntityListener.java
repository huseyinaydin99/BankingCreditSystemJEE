package tr.com.huseyinaydin.persistence.audit;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tr.com.huseyinaydin.domain.audit.AuditLog;
import tr.com.huseyinaydin.domain.common.BaseEntity;
import tr.com.huseyinaydin.domain.repositories.IAuditLogRepository;
import tr.com.huseyinaydin.sharedkernel.audit.AuditAction;

import java.time.Instant;
import java.util.UUID;

public class JpaAuditEntityListener {

    private static ApplicationContext applicationContext;

    public static void setApplicationContext(ApplicationContext context) {
        applicationContext = context;
    }

    @PostPersist
    public void onPostPersist(Object entity) {
        audit(entity, AuditAction.CREATE);
    }

    @PostUpdate
    public void onPostUpdate(Object entity) {
        audit(entity, AuditAction.UPDATE);
    }

    @PostRemove
    public void onPostRemove(Object entity) {
        audit(entity, AuditAction.DELETE);
    }

    private void audit(Object entity, AuditAction action) {
        if (entity instanceof AuditLog) return;
        if (applicationContext == null) return;

        try {
            BaseEntity<?> baseEntity = (BaseEntity<?>) entity;
            String entityId = baseEntity.getId() != null ? baseEntity.getId().toString() : "UNKNOWN";
            String entityType = entity.getClass().getSimpleName();
            
            // In a real system, we'd get the actual user ID from SecurityContext. 
            // Since we can't easily access it here without coupling to infrastructure, 
            // we use a system constant or rely on an async audit event publisher.
            UUID systemUserId = UUID.nameUUIDFromBytes("SYSTEM".getBytes());

            AuditLog log = new AuditLog(
                    entityId,
                    entityType,
                    action,
                    systemUserId,
                    Instant.now(),
                    "127.0.0.1",
                    "{}", // old value serialization skipped for brevity
                    "{}"  // new value serialization skipped for brevity
            );

            // Save via repository in a REQUIRES_NEW transaction to not disrupt current flush
            AuditLogSaver saver = applicationContext.getBean(AuditLogSaver.class);
            saver.saveAuditLog(log);

        } catch (Exception ex) {
            // Log it
        }
    }
}
