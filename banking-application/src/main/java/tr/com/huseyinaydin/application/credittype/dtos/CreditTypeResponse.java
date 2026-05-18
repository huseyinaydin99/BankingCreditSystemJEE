package tr.com.huseyinaydin.application.credittype.dtos;

import tr.com.huseyinaydin.domain.enums.CustomerType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CreditTypeResponse(
        UUID id,
        String name,
        String description,
        CustomerType customerType,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        int minTerm,
        int maxTerm,
        BigDecimal baseInterestRate,
        UUID parentCreditTypeId,
        List<CreditTypeResponse> subCreditTypes,
        LocalDateTime createdDate
) {}
