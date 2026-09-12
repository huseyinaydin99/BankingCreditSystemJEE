package tr.com.huseyinaydin.infrastructure.email;

import io.github.resilience4j.retry.annotation.Retry;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import tr.com.huseyinaydin.application.ports.IEmailNotificationService;
import tr.com.huseyinaydin.domain.enums.CreditApplicationStatus;

import java.util.UUID;

@Service
public class SmtpEmailNotificationService implements IEmailNotificationService {

    private static final Logger log = LoggerFactory.getLogger(SmtpEmailNotificationService.class);

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username:noreply@banking.com}")
    private String fromAddress;

    public SmtpEmailNotificationService(JavaMailSender javaMailSender, TemplateEngine templateEngine) {
        this.javaMailSender = javaMailSender;
        this.templateEngine = templateEngine;
    }

    @Async
    @Retry(name = "emailRetry")
    @Override
    public void sendCreditApplicationStatusChanged(UUID applicationId, String customerEmail, CreditApplicationStatus newStatus, String rejectionReason) {
        log.info("Sending credit application status email to {}", customerEmail);
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromAddress);
            helper.setTo(customerEmail);
            helper.setSubject("Kredi Başvurusu Durum Güncellemesi");

            Context context = new Context();
            context.setVariable("applicationId", applicationId);
            context.setVariable("status", newStatus.name());
            context.setVariable("rejectionReason", rejectionReason);

            String htmlContent = templateEngine.process("credit-application-status", context);
            helper.setText(htmlContent, true);

            javaMailSender.send(message);
            log.info("Credit application status email sent to {}", customerEmail);
        } catch (MessagingException e) {
            log.error("Failed to send credit application status email to {}", customerEmail, e);
            throw new RuntimeException("Email sending failed", e);
        }
    }

    @Async
    @Retry(name = "emailRetry")
    @Override
    public void sendWelcomeEmail(String customerEmail, String fullName) {
        log.info("Sending welcome email to {}", customerEmail);
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromAddress);
            helper.setTo(customerEmail);
            helper.setSubject("Bankamıza Hoş Geldiniz!");

            Context context = new Context();
            context.setVariable("fullName", fullName);

            String htmlContent = templateEngine.process("welcome-email", context);
            helper.setText(htmlContent, true);

            javaMailSender.send(message);
            log.info("Welcome email sent to {}", customerEmail);
        } catch (MessagingException e) {
            log.error("Failed to send welcome email to {}", customerEmail, e);
            throw new RuntimeException("Email sending failed", e);
        }
    }
}
