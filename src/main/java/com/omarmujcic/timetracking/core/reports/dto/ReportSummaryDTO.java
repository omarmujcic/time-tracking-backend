package com.omarmujcic.timetracking.core.reports.dto;

import java.math.BigDecimal;

public record ReportSummaryDTO(
        long totalSeconds,
        BigDecimal totalAmount,
        int entryCount,
        long activeSeconds,
        BigDecimal activeAmount,
        int activeEntryCount,
        String currency
) {
}
