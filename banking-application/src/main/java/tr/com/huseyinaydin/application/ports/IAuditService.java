package tr.com.huseyinaydin.application.ports;

import tr.com.huseyinaydin.sharedkernel.audit.AuditEntry;


public interface IAuditService {
    void record(AuditEntry entry);
}
