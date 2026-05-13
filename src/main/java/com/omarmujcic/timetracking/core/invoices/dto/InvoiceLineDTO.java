package com.omarmujcic.timetracking.core.invoices.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record InvoiceLineDTO(
        UUID id,
        int lineOrder,
        String projectKey,
        UUID projectId,
        String projectName,
        String description,
        long durationSeconds,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal taxRate,
        BigDecimal totalAmount
) {
}
