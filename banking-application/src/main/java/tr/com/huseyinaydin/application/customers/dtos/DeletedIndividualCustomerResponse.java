package tr.com.huseyinaydin.application.customers.dtos;

import java.util.UUID;

public record DeletedIndividualCustomerResponse(
        UUID id,
        boolean deleted,
        String message
) {}
