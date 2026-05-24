package com.omarmujcic.timetracking.core.calendar;

import java.time.LocalDate;
import java.time.YearMonth;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.omarmujcic.timetracking.core.auth.entity.User;
import com.omarmujcic.timetracking.core.calendar.dto.CalendarDayDetailDTO;
import com.omarmujcic.timetracking.core.calendar.dto.CalendarMonthDTO;
import com.omarmujcic.timetracking.core.calendar.dto.CalendarWeekDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;

    @GetMapping("/month")
    public CalendarMonthDTO month(
            @AuthenticationPrincipal User user,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
            @RequestParam(defaultValue = "UTC") String timezone,
            @RequestParam(defaultValue = "false") boolean includeOrganizationEntries
    ) {
        return calendarService.month(user, month, timezone, includeOrganizationEntries);
    }

    @GetMapping("/week")
    public CalendarWeekDTO week(
            @AuthenticationPrincipal User user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "UTC") String timezone,
            @RequestParam(defaultValue = "false") boolean includeOrganizationEntries
    ) {
        return calendarService.week(user, date, timezone, includeOrganizationEntries);
    }

    @GetMapping("/day")
    public CalendarDayDetailDTO day(
            @AuthenticationPrincipal User user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "UTC") String timezone,
            @RequestParam(defaultValue = "false") boolean includeOrganizationEntries
    ) {
        return calendarService.day(user, date, timezone, includeOrganizationEntries);
    }
}
