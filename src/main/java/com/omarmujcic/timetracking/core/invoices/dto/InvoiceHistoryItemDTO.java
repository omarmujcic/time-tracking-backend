package com.omarmujcic.timetracking.core.invoices.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record InvoiceHistoryItemDTO(
        UUID id,
        String invoiceNumber,
        LocalDate issueDate,
        LocalDate dueDate,
        LocalDate periodStart,
        LocalDate periodEnd,
        BigDecimal total,
        String currency,
        OffsetDateTime createdAt
) {
}
