package tr.com.huseyinaydin.sharedkernel.audit;

import java.time.Instant;
import java.util.UUID;

/**
 * Denetim kaydı için değişmez (immutable) value object. AuditBehavior tarafından üretilir,
 * {@code IAuditService.record} ile kalıcılaştırılır. Framework/domain bağımlılığı yoktur.
 *
 * @param entityId    etkilenen kaydın kimliği (String; genelde UUID metni), bilinmiyorsa null
 * @param entityType  etkilenen entity türü (ör. "CorporateCustomer")
 * @param action      işlem türü
 * @param performedBy işlemi yapan kullanıcı kimliği (UUID), anonim ise null
 * @param performedAt işlem zamanı (UTC Instant)
 * @param ipAddress   isteğin geldiği IP adresi, bilinmiyorsa null
 * @param oldValue    önceki durum (JSON), yoksa null
 * @param newValue    yeni durum (JSON), yoksa null
 */
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
