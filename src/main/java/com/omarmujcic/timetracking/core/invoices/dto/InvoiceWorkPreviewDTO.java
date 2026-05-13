package com.omarmujcic.timetracking.core.invoices.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record InvoiceWorkPreviewDTO(
        LocalDate startDate,
        LocalDate endDate,
        List<InvoiceWorkLineDTO> lines,
        BigDecimal subtotal,
        String currency
) {
}
