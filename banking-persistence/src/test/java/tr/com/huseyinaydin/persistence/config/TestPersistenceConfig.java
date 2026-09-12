package tr.com.huseyinaydin.persistence.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
public class TestPersistenceConfig {

    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.h2.Driver");
        config.setJdbcUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        config.setUsername("sa");
        config.setPassword("");
        return new HikariDataSource(config);
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        
        // We only scan persistence package since ORM mappings are in META-INF/orm
        em.setPackagesToScan("tr.com.huseyinaydin.domain");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        
        Properties properties = new Properties();
        properties.setProperty("hibernate.hbm2ddl.auto", "create-drop");
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        properties.setProperty("hibernate.show_sql", "true");
        properties.setProperty("hibernate.format_sql", "true");
        // Instruct Hibernate to load our XML mappings
        // The original PersistenceConfig probably relies on mapping files in META-INF.
        // Spring Boot usually finds them, but we can explicitly list them if needed.
        // Actually, Hibernate auto-discovers META-INF/orm.xml. We have multiple files in META-INF/orm/.
        // Let's set the mapping locations manually just in case:
        em.setMappingResources(
            "META-INF/orm/ApplicationUser.xml",
            "META-INF/orm/AuditLog.xml",
            "META-INF/orm/BaseEntity.xml",
            "META-INF/orm/CorporateCustomer.xml",
            "META-INF/orm/CreditApplication.xml",
            "META-INF/orm/CreditType.xml",
            "META-INF/orm/Customer.xml",
            "META-INF/orm/DomainEventOutbox.xml",
            "META-INF/orm/Entity.xml",
            "META-INF/orm/IndividualCustomer.xml",
            "META-INF/orm/RefreshToken.xml"
        );
        
        em.setJpaProperties(properties);
        return em;
    }

    @Bean
    public PlatformTransactionManager transactionManager(LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory.getObject());
        return transactionManager;
    }
}
