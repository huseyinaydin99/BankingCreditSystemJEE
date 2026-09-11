package tr.com.huseyinaydin.web.health;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@Component("database")
public class DatabaseHealthIndicator implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(DatabaseHealthIndicator.class);

    private static final String VALIDATION_QUERY = "SELECT 1 FROM DUAL";

    private final DataSource dataSource;

    public DatabaseHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(VALIDATION_QUERY)) {

            if (resultSet.next()) {
                Health.Builder builder = Health.up()
                        .withDetail("database", "Oracle")
                        .withDetail("validationQuery", VALIDATION_QUERY)
                        .withDetail("url", sanitizeUrl(connection.getMetaData().getURL()));

                addPoolDetails(builder);
                return builder.build();
            }

            return Health.down()
                    .withDetail("database", "Oracle")
                    .withDetail("error", "Doğrulama sorgusu sonuç döndürmedi: " + VALIDATION_QUERY)
                    .build();

        } catch (Exception ex) {
            log.error("[DatabaseHealthIndicator] Oracle bağlantı hatası: {}", ex.getMessage(), ex);
            return Health.down()
                    .withDetail("database", "Oracle")
                    .withDetail("validationQuery", VALIDATION_QUERY)
                    .withDetail("error", ex.getMessage())
                    .build();
        }
    }

    private void addPoolDetails(Health.Builder builder) {
        if (!(dataSource instanceof HikariDataSource hikariDataSource)) {
            builder.withDetail("connectionPool", "HikariCP değil — pool detayları mevcut değil");
            return;
        }

        builder.withDetail("maxPoolSize", hikariDataSource.getMaximumPoolSize());
        builder.withDetail("minimumIdle", hikariDataSource.getMinimumIdle());

        HikariPoolMXBean poolMXBean = hikariDataSource.getHikariPoolMXBean();
        if (poolMXBean != null) {
            builder.withDetail("activeConnections",         poolMXBean.getActiveConnections())
                   .withDetail("idleConnections",           poolMXBean.getIdleConnections())
                   .withDetail("totalConnections",          poolMXBean.getTotalConnections())
                   .withDetail("threadsAwaitingConnection", poolMXBean.getThreadsAwaitingConnection());
        } else {
            builder.withDetail("poolStats", "Pool henüz başlatılmamış veya mevcut değil");
        }
    }
    private String sanitizeUrl(String url) {
        if (url == null) return "unknown";
        return url.replaceAll("(?i)(:[^@/]+/)[^@/]+@", "$1***@");
    }
}
