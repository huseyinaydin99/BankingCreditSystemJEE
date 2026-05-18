package tr.com.huseyinaydin.application.customers.dtos;

import java.util.UUID;

public record UpdatedCorporateCustomerResponse(
        UUID id,
        String companyName,
        String email,
        String message
) {}
