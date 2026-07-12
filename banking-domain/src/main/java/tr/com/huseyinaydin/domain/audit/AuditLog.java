package tr.com.huseyinaydin.domain.audit;

import tr.com.huseyinaydin.domain.common.BaseEntity;
import tr.com.huseyinaydin.sharedkernel.audit.AuditAction;
// META-INF/orm/AuditLog.xml ile eşleme sağlanmaktadır (annotation yok).

import java.time.Instant;
import java.util.UUID;

/**
 * Denetim izi kaydı. Kim (performedBy), ne zaman (performedAt), ne yaptı (action) ve hangi
 * kayıt üzerinde (entityType/entityId) bilgisini; opsiyonel olarak eski/yeni JSON durumunu
 * ve isteğin IP adresini tutar.
 *
 * {@link BaseEntity}'den türetilir; denetim kayıtları için yumuşak silme (soft delete)
 * kullanılmaz (deletedDate hiç set edilmez).
 */
public class AuditLog extends BaseEntity<UUID> {

    private String entityId;
    private String entityType;
    private AuditAction action;
    private UUID performedBy;
    private Instant performedAt;
    private String ipAddress;
    private String oldValue;
    private String newValue;

    protected AuditLog() {
        super();
    }

    public AuditLog(String entityId, String entityType, AuditAction action,
                    UUID performedBy, Instant performedAt, String ipAddress,
                    String oldValue, String newValue) {
        super();
        this.id = UUID.randomUUID();
        this.entityId = entityId;
        this.entityType = entityType;
        this.action = action;
        this.performedBy = performedBy;
        this.performedAt = performedAt;
        this.ipAddress = ipAddress;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public String getEntityId() { return entityId; }
    public String getEntityType() { return entityType; }
    public AuditAction getAction() { return action; }
    public UUID getPerformedBy() { return performedBy; }
    public Instant getPerformedAt() { return performedAt; }
    public String getIpAddress() { return ipAddress; }
    public String getOldValue() { return oldValue; }
    public String getNewValue() { return newValue; }
}
