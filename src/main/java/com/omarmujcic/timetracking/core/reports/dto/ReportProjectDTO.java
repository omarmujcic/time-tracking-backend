package com.omarmujcic.timetracking.core.reports.dto;

import java.math.BigDecimal;

public record ReportProjectDTO(
        String projectName,
        long totalSeconds,
        BigDecimal totalAmount,
        BigDecimal percentage
) {
}
