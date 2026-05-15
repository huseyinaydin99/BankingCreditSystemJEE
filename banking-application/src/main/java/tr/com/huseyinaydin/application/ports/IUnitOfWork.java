package tr.com.huseyinaydin.application.ports;

import tr.com.huseyinaydin.domain.repositories.IApplicationUserRepository;
import tr.com.huseyinaydin.domain.repositories.ICorporateCustomerRepository;
import tr.com.huseyinaydin.domain.repositories.ICreditApplicationRepository;
import tr.com.huseyinaydin.domain.repositories.ICreditTypeRepository;
import tr.com.huseyinaydin.domain.repositories.IIndividualCustomerRepository;

public interface IUnitOfWork {

    void beginTransaction();

    void commit();

    void rollback();

    boolean isActive();

    IIndividualCustomerRepository individualCustomers();

    ICorporateCustomerRepository corporateCustomers();

    ICreditApplicationRepository creditApplications();

    ICreditTypeRepository creditTypes();

    IApplicationUserRepository applicationUsers();
}
