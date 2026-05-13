package com.omarmujcic.timetracking.core.reports;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.omarmujcic.timetracking.core.auth.entity.User;
import com.omarmujcic.timetracking.core.reports.dto.ReportFilterOptionsDTO;
import com.omarmujcic.timetracking.core.reports.dto.TimeReportDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/time")
    public TimeReportDTO timeReport(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "MONTHLY") ReportView view,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "UTC") String timezone,
            @RequestParam(required = false) List<UUID> userIds,
            @RequestParam(required = false) List<String> projectNames,
            @RequestParam(required = false) List<UUID> taskIds,
            @RequestParam(defaultValue = "false") boolean includeNoTask,
            @RequestParam(required = false) BigDecimal minRate,
            @RequestParam(required = false) BigDecimal maxRate,
            @RequestParam(required = false) String description
    ) {
        return reportService.timeReport(user, view, startDate, endDate, timezone, userIds, projectNames, taskIds,
                includeNoTask, minRate, maxRate, description);
    }

    @GetMapping("/filter-options")
    public ReportFilterOptionsDTO filterOptions(@AuthenticationPrincipal User user) {
        return reportService.filterOptions(user);
    }
}
