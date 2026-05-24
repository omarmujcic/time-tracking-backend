package com.omarmujcic.timetracking.core.calendar.dto;

import java.time.LocalDate;
import java.util.List;

public record CalendarDayDTO(
        LocalDate date,
        long totalSeconds,
        int entryCount,
        boolean active,
        List<CalendarProjectTotalDTO> projectTotals,
        List<CalendarUserTotalDTO> userTotals
) {
}
