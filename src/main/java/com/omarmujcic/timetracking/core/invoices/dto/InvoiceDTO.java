package com.omarmujcic.timetracking.core.invoices.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.omarmujcic.timetracking.core.workspace.entity.WorkspaceType;

public record InvoiceDTO(
        UUID id,
        WorkspaceType workspaceType,
        UUID organizationId,
        String invoiceNumber,
        LocalDate issueDate,
        LocalDate dueDate,
        LocalDate periodStart,
        LocalDate periodEnd,
        InvoicePartyDTO from,
        InvoicePartyDTO to,
        List<InvoiceLineDTO> lines,
        BigDecimal subtotal,
        String taxLabel,
        BigDecimal taxRate,
        BigDecimal taxAmount,
        BigDecimal total,
        String currency,
        String terms,
        OffsetDateTime createdAt
) {
}
