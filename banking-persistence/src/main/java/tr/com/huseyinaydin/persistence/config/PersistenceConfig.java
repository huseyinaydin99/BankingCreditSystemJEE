package tr.com.huseyinaydin.persistence.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
public class PersistenceConfig {

    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:oracle:thin:@localhost:1521/FREEPDB1");
        config.setDriverClassName("oracle.jdbc.OracleDriver");
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(30_000);
        config.setIdleTimeout(600_000);
        config.setMaxLifetime(1_800_000);
        config.setConnectionTestQuery("SELECT 1 FROM DUAL");
        return new HikariDataSource(config);
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource);
        emf.setPackagesToScan("tr.com.huseyinaydin.domain");
        emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        emf.setJpaProperties(hibernateProperties());
        return emf;
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    private Properties hibernateProperties() {
        Properties props = new Properties();
        props.setProperty("hibernate.dialect", "org.hibernate.dialect.OracleDialect");
        props.setProperty("hibernate.hbm2ddl.auto", "validate");
        props.setProperty("hibernate.show_sql", "false");
        props.setProperty("hibernate.format_sql", "true");
        props.setProperty("hibernate.jdbc.batch_size", "50");
        props.setProperty("hibernate.order_inserts", "true");
        props.setProperty("hibernate.order_updates", "true");
        props.setProperty("hibernate.generate_statistics", "false");
        props.setProperty("hibernate.connection.provider_disables_autocommit", "true");
        return props;
    }
}
