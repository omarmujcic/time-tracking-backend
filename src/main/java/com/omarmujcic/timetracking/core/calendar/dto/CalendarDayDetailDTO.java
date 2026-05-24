package com.omarmujcic.timetracking.core.calendar.dto;

import java.time.LocalDate;
import java.util.List;

import com.omarmujcic.timetracking.core.calendar.CalendarViewerMode;

public record CalendarDayDetailDTO(
        CalendarViewerMode viewerMode,
        LocalDate date,
        CalendarSummaryDTO summary,
        List<CalendarProjectTotalDTO> projectTotals,
        List<CalendarUserTotalDTO> userTotals,
        List<CalendarEntryDTO> entries
) {
}
