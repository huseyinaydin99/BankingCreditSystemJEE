package tr.com.huseyinaydin.infrastructure.uow;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.ports.IUnitOfWork;
import tr.com.huseyinaydin.domain.repositories.IApplicationUserRepository;
import tr.com.huseyinaydin.domain.repositories.ICorporateCustomerRepository;
import tr.com.huseyinaydin.domain.repositories.ICreditApplicationRepository;
import tr.com.huseyinaydin.domain.repositories.ICreditTypeRepository;
import tr.com.huseyinaydin.domain.repositories.IIndividualCustomerRepository;
import tr.com.huseyinaydin.infrastructure.repositories.ApplicationUserJpaRepository;
import tr.com.huseyinaydin.infrastructure.repositories.CorporateCustomerJpaRepository;
import tr.com.huseyinaydin.infrastructure.repositories.CreditApplicationJpaRepository;
import tr.com.huseyinaydin.infrastructure.repositories.CreditTypeJpaRepository;
import tr.com.huseyinaydin.infrastructure.repositories.IndividualCustomerJpaRepository;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class JpaUnitOfWork implements IUnitOfWork {

    private final EntityManager entityManager;

    private IIndividualCustomerRepository individualCustomers;
    private ICorporateCustomerRepository corporateCustomers;
    private ICreditApplicationRepository creditApplications;
    private ICreditTypeRepository creditTypes;
    private IApplicationUserRepository applicationUsers;

    public JpaUnitOfWork(EntityManagerFactory entityManagerFactory) {
        this.entityManager = entityManagerFactory.createEntityManager();
    }

    @Override
    public void beginTransaction() {
        EntityTransaction tx = entityManager.getTransaction();
        if (!tx.isActive()) {
            tx.begin();
        }
    }

    @Override
    public void commit() {
        entityManager.getTransaction().commit();
    }

    @Override
    public void rollback() {
        EntityTransaction tx = entityManager.getTransaction();
        if (tx.isActive()) {
            tx.rollback();
        }
    }

    @Override
    public boolean isActive() {
        return entityManager.getTransaction().isActive();
    }

    @Override
    public IIndividualCustomerRepository individualCustomers() {
        if (individualCustomers == null) {
            individualCustomers = new IndividualCustomerJpaRepository(entityManager);
        }
        return individualCustomers;
    }

    @Override
    public ICorporateCustomerRepository corporateCustomers() {
        if (corporateCustomers == null) {
            corporateCustomers = new CorporateCustomerJpaRepository(entityManager);
        }
        return corporateCustomers;
    }

    @Override
    public ICreditApplicationRepository creditApplications() {
        if (creditApplications == null) {
            creditApplications = new CreditApplicationJpaRepository(entityManager);
        }
        return creditApplications;
    }

    @Override
    public ICreditTypeRepository creditTypes() {
        if (creditTypes == null) {
            creditTypes = new CreditTypeJpaRepository(entityManager);
        }
        return creditTypes;
    }

    @Override
    public IApplicationUserRepository applicationUsers() {
        if (applicationUsers == null) {
            applicationUsers = new ApplicationUserJpaRepository(entityManager);
        }
        return applicationUsers;
    }
}
