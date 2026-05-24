package com.omarmujcic.timetracking.core.calendar.dto;

import java.util.List;

import com.omarmujcic.timetracking.core.calendar.CalendarViewerMode;

public record CalendarMonthDTO(
        CalendarViewerMode viewerMode,
        CalendarSummaryDTO summary,
        List<CalendarDayDTO> days
) {
}
