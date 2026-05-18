package tr.com.huseyinaydin.application.customers.dtos;

import java.util.UUID;

public record CreatedIndividualCustomerResponse(
        UUID id,
        String firstName,
        String lastName,
        String nationalId,
        String email,
        String message
) {}
