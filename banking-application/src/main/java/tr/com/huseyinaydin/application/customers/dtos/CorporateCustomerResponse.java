package tr.com.huseyinaydin.application.customers.dtos;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record CorporateCustomerResponse(
        UUID id,
        String companyName,
        String taxNumber,
        String taxOffice,
        String companyRegistrationNumber,
        String authorizedPersonName,
        LocalDate companyFoundationDate,
        String phoneNumber,
        String email,
        boolean isActive,
        LocalDateTime createdDate
) {}
