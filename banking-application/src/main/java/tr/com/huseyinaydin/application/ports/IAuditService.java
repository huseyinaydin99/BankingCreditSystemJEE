package tr.com.huseyinaydin.application.ports;

import tr.com.huseyinaydin.sharedkernel.audit.AuditEntry;

/**
 * Denetim kaydı yazma port'u. Implementasyonu altyapı katmanında yer alır (JpaAuditService).
 * Kayıt, asıl işlem transaction'ının dışında/sonrasında yapılır.
 */
public interface IAuditService {
    void record(AuditEntry entry);
}
