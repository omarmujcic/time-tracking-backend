package com.omarmujcic.timetracking.core.calendar.dto;

import java.time.LocalDate;
import java.util.List;

import com.omarmujcic.timetracking.core.calendar.CalendarViewerMode;

public record CalendarWeekDTO(
        CalendarViewerMode viewerMode,
        LocalDate startDate,
        LocalDate endDate,
        CalendarSummaryDTO summary,
        List<CalendarDayDTO> days,
        List<CalendarEntryDTO> entries
) {
}
