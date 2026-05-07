package com.omarmujcic.timetracking.core.reports.dto;

import java.math.BigDecimal;

public record ReportBucketDTO(
        String key,
        String label,
        long totalSeconds,
        BigDecimal totalAmount
) {
}
