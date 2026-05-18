package tr.com.huseyinaydin.application.customers.dtos;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record IndividualCustomerResponse(
        UUID id,
        String firstName,
        String lastName,
        String nationalId,
        LocalDate dateOfBirth,
        String motherName,
        String fatherName,
        String phoneNumber,
        String email,
        String address,
        boolean isActive,
        LocalDateTime createdDate,
        LocalDateTime updatedDate
) {}
