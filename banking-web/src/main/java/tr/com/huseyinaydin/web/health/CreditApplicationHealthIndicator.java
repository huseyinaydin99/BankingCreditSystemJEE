package tr.com.huseyinaydin.web.health;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.domain.enums.CreditApplicationStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component("creditApplication")
public class CreditApplicationHealthIndicator implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(CreditApplicationHealthIndicator.class);

    private static final long DEGRADED_THRESHOLD = 100L;

    private static final long LOOK_BACK_HOURS = 1L;

    static final Status DEGRADED = new Status("DEGRADED");

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final EntityManagerFactory entityManagerFactory;

    public CreditApplicationHealthIndicator(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public Health health() {
        LocalDateTime checkedAt = LocalDateTime.now();
        LocalDateTime since     = checkedAt.minusHours(LOOK_BACK_HOURS);

        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            long pendingCount = countPendingApplications(em, since);

            if (pendingCount >= DEGRADED_THRESHOLD) {
                log.warn("[CreditApplicationHealthIndicator] DEGRADED — " +
                         "Son {} saatte {} PENDING başvuru (eşik: {})",
                         LOOK_BACK_HOURS, pendingCount, DEGRADED_THRESHOLD);

                return Health.status(DEGRADED)
                        .withDetail("pendingCount",    pendingCount)
                        .withDetail("threshold",       DEGRADED_THRESHOLD)
                        .withDetail("lookBackHours",   LOOK_BACK_HOURS)
                        .withDetail("message",
                                "Son " + LOOK_BACK_HOURS + " saatte yüksek sayıda " +
                                "PENDING kredi başvurusu tespit edildi. " +
                                "Operasyonel müdahale gerekebilir.")
                        .withDetail("checkedAt", checkedAt.format(FORMATTER))
                        .withDetail("periodStart", since.format(FORMATTER))
                        .build();
            }

            return Health.up()
                    .withDetail("pendingCount",  pendingCount)
                    .withDetail("threshold",     DEGRADED_THRESHOLD)
                    .withDetail("lookBackHours", LOOK_BACK_HOURS)
                    .withDetail("checkedAt",     checkedAt.format(FORMATTER))
                    .withDetail("periodStart",   since.format(FORMATTER))
                    .build();

        } catch (Exception ex) {
            log.error("[CreditApplicationHealthIndicator] PENDING başvuru sorgusu başarısız: {}",
                      ex.getMessage(), ex);
            return Health.down()
                    .withDetail("error",     ex.getMessage())
                    .withDetail("checkedAt", checkedAt.format(FORMATTER))
                    .build();
        } finally {
            em.close();
        }
    }

    private long countPendingApplications(EntityManager em, LocalDateTime since) {
        return em.createQuery(
                "SELECT COUNT(ca) " +
                "FROM CreditApplication ca " +
                "WHERE ca.status = :status " +
                "AND ca.createdDate >= :since " +
                "AND ca.deletedDate IS NULL",
                Long.class)
                .setParameter("status", CreditApplicationStatus.PENDING)
                .setParameter("since", since)
                .getSingleResult();
    }
}
