package tr.com.huseyinaydin.application.ports;

import tr.com.huseyinaydin.domain.enums.CreditApplicationStatus;
import java.util.UUID;

public interface IEmailNotificationService {
    void sendCreditApplicationStatusChanged(UUID applicationId, String customerEmail, CreditApplicationStatus newStatus, String rejectionReason);
    void sendWelcomeEmail(String customerEmail, String fullName);
}
