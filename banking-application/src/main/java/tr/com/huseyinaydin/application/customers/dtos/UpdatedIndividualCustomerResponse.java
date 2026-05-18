package tr.com.huseyinaydin.application.customers.dtos;

import java.util.UUID;

public record UpdatedIndividualCustomerResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String message
) {}
