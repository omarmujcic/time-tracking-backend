package com.omarmujcic.timetracking.core.reports.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ReportBucketSegmentDTO(
        UUID taskId,
        String taskName,
        String projectName,
        long totalSeconds,
        BigDecimal totalAmount
) {
}
