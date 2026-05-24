package com.omarmujcic.timetracking.core.calendar.dto;

import java.math.BigDecimal;

public record CalendarProjectTotalDTO(
        String projectName,
        long totalSeconds,
        BigDecimal totalAmount
) {
}
