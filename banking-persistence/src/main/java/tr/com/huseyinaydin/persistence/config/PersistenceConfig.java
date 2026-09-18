package tr.com.huseyinaydin.persistence.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@org.springframework.context.annotation.ComponentScan(basePackages = "tr.com.huseyinaydin.persistence")
public class PersistenceConfig {

    @Autowired
    private Environment env;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Bean
    @Profile({"dev", "test"})
    public DataSource devDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl(dbUrl);
        dataSource.setUsername(dbUsername);
        dataSource.setPassword(dbPassword);
        return dataSource;
    }

    @Bean
    public org.springframework.jdbc.core.JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new org.springframework.jdbc.core.JdbcTemplate(dataSource);
    }

    @Bean
    @Profile("prod")
    public DataSource prodDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dbUrl);
        config.setDriverClassName("oracle.jdbc.OracleDriver");
        config.setUsername(dbUsername);
        config.setPassword(dbPassword);
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
        emf.setMappingResources(
            "META-INF/orm/BaseEntity.xml",
            "META-INF/orm/Entity.xml",
            "META-INF/orm/Customer.xml",
            "META-INF/orm/IndividualCustomer.xml",
            "META-INF/orm/CorporateCustomer.xml",
            "META-INF/orm/CreditType.xml",
            "META-INF/orm/CreditApplication.xml",
            "META-INF/orm/ApplicationUser.xml",
            "META-INF/orm/RefreshToken.xml",
            "META-INF/orm/AuditLog.xml",
            "META-INF/orm/OperationClaim.xml",
            "META-INF/orm/UserOperationClaim.xml"
        );
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
        
        if (env.acceptsProfiles(org.springframework.core.env.Profiles.of("prod"))) {
            props.setProperty("hibernate.dialect", "org.hibernate.dialect.OracleDialect");
            props.setProperty("hibernate.hbm2ddl.auto", "validate");
            props.setProperty("hibernate.show_sql", "false");
        } else {
            props.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
            props.setProperty("hibernate.hbm2ddl.auto", "create-drop");
            props.setProperty("hibernate.show_sql", "true");
        }
        
        props.setProperty("hibernate.format_sql", "true");
        props.setProperty("hibernate.jdbc.batch_size", "50");
        props.setProperty("hibernate.order_inserts", "true");
        props.setProperty("hibernate.order_updates", "true");
        props.setProperty("hibernate.generate_statistics", "false");
        props.setProperty("hibernate.connection.provider_disables_autocommit", "true");
        return props;
    }
}
