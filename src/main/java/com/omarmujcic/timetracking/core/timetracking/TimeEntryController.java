package com.omarmujcic.timetracking.core.timetracking;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.omarmujcic.timetracking.core.auth.entity.User;
import com.omarmujcic.timetracking.core.timetracking.dto.CreateTimeEntryRequestDTO;
import com.omarmujcic.timetracking.core.timetracking.dto.StartTimerRequestDTO;
import com.omarmujcic.timetracking.core.timetracking.dto.TimeEntryResponseDTO;
import com.omarmujcic.timetracking.core.timetracking.dto.TimeEntrySummaryDTO;
import com.omarmujcic.timetracking.core.timetracking.dto.UpdateTimeEntryRequestDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/time-entries")
@RequiredArgsConstructor
public class TimeEntryController {

    private final TimeEntryService timeEntryService;

    @GetMapping
    public List<TimeEntryResponseDTO> list(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate day,
            @RequestParam(required = false) String project,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) List<String> projectNames,
            @RequestParam(required = false) List<UUID> userIds
    ) {
        return timeEntryService.list(user, month, day, project, userId, projectNames, userIds);
    }

    @GetMapping("/summary")
    public TimeEntrySummaryDTO summary(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate day,
            @RequestParam(required = false) String project,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) List<String> projectNames,
            @RequestParam(required = false) List<UUID> userIds
    ) {
        return timeEntryService.summary(user, month, day, project, userId, projectNames, userIds);
    }

    @PostMapping("/start")
    public TimeEntryResponseDTO start(@AuthenticationPrincipal User user,
            @Valid @RequestBody StartTimerRequestDTO request) {
        return timeEntryService.start(user, request);
    }

    @PostMapping("/{id}/stop")
    public TimeEntryResponseDTO stop(@AuthenticationPrincipal User user, @PathVariable UUID id) {
        return timeEntryService.stop(user, id);
    }

    @PostMapping
    public TimeEntryResponseDTO create(@AuthenticationPrincipal User user,
            @Valid @RequestBody CreateTimeEntryRequestDTO request) {
        return timeEntryService.create(user, request);
    }

    @PutMapping("/{id}")
    public TimeEntryResponseDTO update(@AuthenticationPrincipal User user, @PathVariable UUID id,
            @Valid @RequestBody UpdateTimeEntryRequestDTO request) {
        return timeEntryService.update(user, id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@AuthenticationPrincipal User user, @PathVariable UUID id) {
        timeEntryService.delete(user, id);
    }
}
