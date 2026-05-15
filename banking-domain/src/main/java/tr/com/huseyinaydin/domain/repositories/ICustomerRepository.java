package tr.com.huseyinaydin.domain.repositories;

import tr.com.huseyinaydin.domain.customer.Customer;

import java.util.UUID;

public interface ICustomerRepository<TCustomer extends Customer>
        extends IAsyncRepository<TCustomer, UUID> {
}
