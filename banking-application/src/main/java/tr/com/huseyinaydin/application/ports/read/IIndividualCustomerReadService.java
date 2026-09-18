package tr.com.huseyinaydin.application.ports.read;

import tr.com.huseyinaydin.application.customers.dtos.IndividualCustomerResponse;
import java.util.Optional;
import java.util.UUID;

public interface IIndividualCustomerReadService {
    Optional<IndividualCustomerResponse> getById(UUID id);
}
