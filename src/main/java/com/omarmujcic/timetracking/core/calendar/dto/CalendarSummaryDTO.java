package com.omarmujcic.timetracking.core.calendar.dto;

import java.math.BigDecimal;

public record CalendarSummaryDTO(
        long totalSeconds,
        BigDecimal totalAmount,
        int workedDayCount,
        int entryCount,
        long activeSeconds,
        int activeEntryCount,
        String currency
) {
}
