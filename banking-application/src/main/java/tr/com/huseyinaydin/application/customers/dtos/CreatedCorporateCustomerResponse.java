package tr.com.huseyinaydin.application.customers.dtos;

import java.util.UUID;

public record CreatedCorporateCustomerResponse(
        UUID id,
        String companyName,
        String taxNumber,
        String message
) {}
