package com.omarmujcic.timetracking.core.reports.dto;

import java.math.BigDecimal;
import java.util.List;

public record ReportBucketDTO(
        String key,
        String label,
        long totalSeconds,
        BigDecimal totalAmount,
        List<ReportBucketSegmentDTO> taskSegments
) {
}
