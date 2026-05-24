package com.omarmujcic.timetracking.core.calendar.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record CalendarEntryDTO(
        UUID id,
        UUID userId,
        String username,
        String displayName,
        UUID projectId,
        String projectName,
        UUID taskId,
        String taskName,
        BigDecimal hourlyRate,
        String currency,
        OffsetDateTime startedAt,
        OffsetDateTime endedAt,
        long durationSeconds,
        BigDecimal billableAmount,
        boolean active
) {
}
