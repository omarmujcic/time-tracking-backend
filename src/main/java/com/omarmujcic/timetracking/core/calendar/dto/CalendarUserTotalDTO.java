package com.omarmujcic.timetracking.core.calendar.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CalendarUserTotalDTO(
        UUID userId,
        String username,
        String displayName,
        long totalSeconds,
        BigDecimal totalAmount
) {
}
