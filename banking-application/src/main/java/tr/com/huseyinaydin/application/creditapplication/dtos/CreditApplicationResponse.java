package tr.com.huseyinaydin.application.creditapplication.dtos;

import tr.com.huseyinaydin.domain.enums.CreditApplicationStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreditApplicationResponse(
        UUID id,
        UUID customerId,
        String customerName,
        UUID creditTypeId,
        String creditTypeName,
        BigDecimal requestedAmount,
        int requestedTerm,
        BigDecimal approvedAmount,
        BigDecimal interestRate,
        BigDecimal monthlyPayment,
        BigDecimal totalPayment,
        CreditApplicationStatus status,
        String rejectionReason,
        LocalDateTime createdDate
) {}
