package tr.com.huseyinaydin.application.stubs;

import tr.com.huseyinaydin.application.ports.IUnitOfWork;
import tr.com.huseyinaydin.domain.repositories.*;

public class InMemoryUnitOfWork implements IUnitOfWork {

    private final IIndividualCustomerRepository individualCustomers = new InMemoryIndividualCustomerRepository();
    private final ICorporateCustomerRepository corporateCustomers = new InMemoryCorporateCustomerRepository();
    private final ICreditApplicationRepository creditApplications = new InMemoryCreditApplicationRepository();
    private final ICreditTypeRepository creditTypes = new InMemoryCreditTypeRepository();
    private final IApplicationUserRepository applicationUsers = new InMemoryApplicationUserRepository();
    
    private boolean active = false;

    @Override
    public void beginTransaction() {
        this.active = true;
    }

    @Override
    public void commit() {
        this.active = false;
    }

    @Override
    public void rollback() {
        this.active = false;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public IIndividualCustomerRepository individualCustomers() { return individualCustomers; }

    @Override
    public ICorporateCustomerRepository corporateCustomers() { return corporateCustomers; }

    @Override
    public ICreditApplicationRepository creditApplications() { return creditApplications; }

    @Override
    public ICreditTypeRepository creditTypes() { return creditTypes; }

    @Override
    public IApplicationUserRepository applicationUsers() { return applicationUsers; }
}
