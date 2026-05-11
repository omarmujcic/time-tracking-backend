package com.omarmujcic.timetracking.core.reports.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ReportEntryDTO(
        UUID id,
        UUID userId,
        String username,
        String displayName,
        String projectName,
        UUID taskId,
        String taskName,
        BigDecimal hourlyRate,
        String currency,
        OffsetDateTime startedAt,
        OffsetDateTime endedAt,
        long durationSeconds,
        BigDecimal billableAmount,
        boolean active,
        String groupKey,
        String groupLabel
) {
}
