package tr.com.huseyinaydin.application.customers.dtos;

import java.util.UUID;

public record DeletedCorporateCustomerResponse(
        UUID id,
        boolean deleted,
        String message
) {}
