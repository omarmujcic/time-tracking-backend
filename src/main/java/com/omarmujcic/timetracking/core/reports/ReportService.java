package com.omarmujcic.timetracking.core.reports;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.omarmujcic.timetracking.core.auth.entity.User;
import com.omarmujcic.timetracking.core.auth.repository.UserRepository;
import com.omarmujcic.timetracking.core.reports.dto.ReportBucketDTO;
import com.omarmujcic.timetracking.core.reports.dto.ReportBucketSegmentDTO;
import com.omarmujcic.timetracking.core.reports.dto.ReportEntryDTO;
import com.omarmujcic.timetracking.core.reports.dto.ReportFilterOptionsDTO;
import com.omarmujcic.timetracking.core.reports.dto.ReportProjectDTO;
import com.omarmujcic.timetracking.core.reports.dto.ReportSummaryDTO;
import com.omarmujcic.timetracking.core.reports.dto.TimeReportDTO;
import com.omarmujcic.timetracking.core.reports.mapper.ReportMapper;
import com.omarmujcic.timetracking.core.timetracking.entity.TimeEntry;
import com.omarmujcic.timetracking.core.timetracking.repository.TimeEntryRepository;
import com.omarmujcic.timetracking.core.workspace.WorkspaceService;
import com.omarmujcic.timetracking.core.workspace.entity.OrganizationMember;
import com.omarmujcic.timetracking.core.workspace.entity.OrganizationRole;
import com.omarmujcic.timetracking.core.workspace.entity.WorkspaceType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportService {

    private static final String CURRENCY = "EUR";
    private static final DateTimeFormatter DAY_KEY = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DAY_LABEL = DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH);
    private static final DateTimeFormatter MONTH_KEY = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final DateTimeFormatter MONTH_LABEL = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH);

    private final TimeEntryRepository timeEntryRepository;
    private final UserRepository userRepository;
    private final ReportMapper reportMapper;
    private final WorkspaceService workspaceService;

    @Transactional(readOnly = true)
    public TimeReportDTO timeReport(User user, ReportView view, LocalDate startDate, LocalDate endDate, String timezone,
            List<UUID> userIds, List<String> projectNames, List<UUID> taskIds, boolean includeNoTask,
            BigDecimal minRate, BigDecimal maxRate, String description) {
        if (endDate.isBefore(startDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date cannot be before start date");
        }

        ZoneId zone = zone(timezone);
        OffsetDateTime now = now();
        Instant rangeStart = startDate.atStartOfDay(zone).toInstant();
        Instant rangeEnd = endDate.plusDays(1).atStartOfDay(zone).toInstant();
        Set<String> normalizedProjects = normalizedProjects(projectNames);
        Set<UUID> selectedUsers = userIds == null ? Set.of() : new LinkedHashSet<>(userIds);
        Set<UUID> selectedTasks = taskIds == null ? Set.of() : new LinkedHashSet<>(taskIds);
        boolean taskFilterActive = !selectedTasks.isEmpty() || includeNoTask;

        List<TimeEntry> entries = visibleEntries(user).stream()
            .filter(entry -> selectedUsers.isEmpty() || selectedUsers.contains(entry.getUser().getId()))
            .filter(entry -> normalizedProjects.isEmpty()
                || normalizedProjects.contains(projectName(entry).trim().toLowerCase(Locale.ROOT)))
            .filter(entry -> !taskFilterActive || taskMatches(entry, selectedTasks, includeNoTask))
            .filter(entry -> minRate == null || entry.getHourlyRate().compareTo(minRate) >= 0)
            .filter(entry -> maxRate == null || entry.getHourlyRate().compareTo(maxRate) <= 0)
            .filter(entry -> overlaps(entry.getStartedAt().toInstant(), endInstant(entry, now), rangeStart, rangeEnd))
            .toList();

        List<BucketWindow> windows = bucketWindows(view, startDate, endDate, zone);
        Map<String, Totals> bucketTotals = windows.stream()
            .collect(Collectors.toMap(BucketWindow::key, ignored -> new Totals(), (first, second) -> first, LinkedHashMap::new));
        Map<String, Map<TaskSegmentKey, TaskSegmentTotals>> bucketTaskTotals = windows.stream()
            .collect(Collectors.toMap(BucketWindow::key, ignored -> new LinkedHashMap<>(), (first, second) -> first,
                    LinkedHashMap::new));
        Map<String, ProjectTotals> projectTotals = new LinkedHashMap<>();
        List<ReportEntryDTO> reportEntries = new ArrayList<>();
        Totals completedTotals = new Totals();
        Totals activeTotals = new Totals();
        int completedEntryCount = 0;
        int activeEntryCount = 0;

        for (TimeEntry entry : entries) {
            Instant entryStart = entry.getStartedAt().toInstant();
            Instant effectiveEnd = endInstant(entry, now);
            long clippedSeconds = overlapSeconds(entryStart, effectiveEnd, rangeStart, rangeEnd);
            BigDecimal clippedAmount = amount(entry.getHourlyRate(), clippedSeconds);

            if (entry.getEndedAt() == null) {
                activeTotals.add(clippedSeconds, clippedAmount);
                activeEntryCount++;
            } else {
                completedTotals.add(clippedSeconds, clippedAmount);
                completedEntryCount++;
                addBucketTotals(entry, windows, bucketTotals, bucketTaskTotals);
                projectTotals.computeIfAbsent(projectName(entry), ProjectTotals::new).add(clippedSeconds, clippedAmount);
            }

            LocalDate entryDate = entry.getStartedAt().atZoneSameInstant(zone).toLocalDate();
            reportEntries.add(reportMapper.toEntryDTO(entry, clippedSeconds, clippedAmount, entry.getEndedAt() == null,
                    entryDate));
        }

        List<ReportBucketDTO> buckets = windows.stream()
            .map(window -> {
                Totals totals = bucketTotals.get(window.key());
                List<ReportBucketSegmentDTO> taskSegments = bucketTaskTotals.get(window.key()).values().stream()
                    .sorted(Comparator.comparingLong(TaskSegmentTotals::seconds).reversed()
                        .thenComparing(TaskSegmentTotals::label, String.CASE_INSENSITIVE_ORDER))
                    .map(segment -> new ReportBucketSegmentDTO(segment.taskId(), segment.taskName(),
                            segment.projectName(), segment.seconds(), segment.amount()))
                    .toList();
                return new ReportBucketDTO(window.key(), window.label(), totals.seconds, totals.amount, taskSegments);
            })
            .toList();

        long projectSeconds = Math.max(1, completedTotals.seconds);
        List<ReportProjectDTO> projects = projectTotals.values().stream()
            .sorted(Comparator.comparingLong(ProjectTotals::seconds).reversed().thenComparing(ProjectTotals::projectName))
            .map(project -> new ReportProjectDTO(
                    project.projectName,
                    project.seconds,
                    project.amount,
                    BigDecimal.valueOf(project.seconds)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(projectSeconds), 2, RoundingMode.HALF_UP)
            ))
            .toList();

        ReportSummaryDTO summary = new ReportSummaryDTO(
                completedTotals.seconds,
                completedTotals.amount,
                completedEntryCount,
                activeTotals.seconds,
                activeTotals.amount,
                activeEntryCount,
                CURRENCY
        );
        return new TimeReportDTO(summary, buckets, projects, reportEntries);
    }

    @Transactional(readOnly = true)
    public ReportFilterOptionsDTO filterOptions(User user) {
        List<User> visibleUsers = visibleEntries(user).stream()
            .map(TimeEntry::getUser)
            .collect(Collectors.toMap(User::getId, option -> option, (first, second) -> first, LinkedHashMap::new))
            .values()
            .stream()
            .toList();
        if (visibleUsers.isEmpty()) {
            visibleUsers = List.of(user);
        }
        List<ReportFilterOptionsDTO.ReportUserOptionDTO> users = visibleUsers.stream()
            .sorted(Comparator.comparing(User::getDisplayName, String.CASE_INSENSITIVE_ORDER))
            .map(option -> new ReportFilterOptionsDTO.ReportUserOptionDTO(option.getId(), option.getUsername(),
                    option.getDisplayName()))
            .toList();

        List<TimeEntry> entries = visibleEntries(user);
        List<String> projects = entries.stream()
            .map(this::projectName)
            .filter(project -> project != null && !project.isBlank())
            .collect(Collectors.toCollection(LinkedHashSet::new))
            .stream()
            .sorted(String.CASE_INSENSITIVE_ORDER)
            .toList();
        List<ReportFilterOptionsDTO.ReportTaskOptionDTO> tasks = entries.stream()
            .filter(entry -> entry.getTask() != null)
            .collect(Collectors.toMap(entry -> entry.getTask().getId(), entry -> reportMapper.toTaskOptionDTO(
                    entry.getTask(), projectName(entry)), (first, second) -> first, LinkedHashMap::new))
            .values()
            .stream()
            .sorted(Comparator.comparing(ReportFilterOptionsDTO.ReportTaskOptionDTO::projectName,
                    String.CASE_INSENSITIVE_ORDER)
                .thenComparing(ReportFilterOptionsDTO.ReportTaskOptionDTO::name, String.CASE_INSENSITIVE_ORDER))
            .toList();
        boolean hasNoTask = entries.stream().anyMatch(entry -> entry.getTask() == null);
        List<BigDecimal> rates = entries.stream()
            .map(TimeEntry::getHourlyRate)
            .collect(Collectors.toCollection(LinkedHashSet::new))
            .stream()
            .sorted()
            .toList();

        return new ReportFilterOptionsDTO(users, projects, tasks, rates, hasNoTask);
    }

    private void addBucketTotals(TimeEntry entry, List<BucketWindow> windows, Map<String, Totals> bucketTotals,
            Map<String, Map<TaskSegmentKey, TaskSegmentTotals>> bucketTaskTotals) {
        Instant entryStart = entry.getStartedAt().toInstant();
        Instant entryEnd = entry.getEndedAt().toInstant();
        for (BucketWindow window : windows) {
            long seconds = overlapSeconds(entryStart, entryEnd, window.start(), window.end());
            if (seconds > 0) {
                BigDecimal segmentAmount = amount(entry.getHourlyRate(), seconds);
                bucketTotals.get(window.key()).add(seconds, segmentAmount);
                TaskSegmentKey key = taskSegmentKey(entry);
                bucketTaskTotals.get(window.key())
                    .computeIfAbsent(key, TaskSegmentTotals::new)
                    .add(seconds, segmentAmount);
            }
        }
    }

    private List<BucketWindow> bucketWindows(ReportView view, LocalDate startDate, LocalDate endDate, ZoneId zone) {
        if (view == ReportView.DAILY) {
            LocalDateTime dayStart = startDate.atStartOfDay();
            return java.util.stream.IntStream.range(0, 24)
                .mapToObj(hour -> {
                    LocalDateTime start = dayStart.plusHours(hour);
                    return new BucketWindow(String.format("%02d:00", hour), String.format("%02d:00", hour),
                            start.atZone(zone).toInstant(), start.plusHours(1).atZone(zone).toInstant());
                })
                .toList();
        }

        boolean monthlyBuckets = view == ReportView.YEARLY
            || (view == ReportView.CUSTOM && ChronoUnit.DAYS.between(startDate, endDate) > 92);
        if (monthlyBuckets) {
            List<BucketWindow> windows = new ArrayList<>();
            YearMonth current = YearMonth.from(startDate);
            YearMonth last = YearMonth.from(endDate);
            while (!current.isAfter(last)) {
                LocalDate monthStart = current.atDay(1);
                LocalDate monthEnd = current.plusMonths(1).atDay(1);
                windows.add(new BucketWindow(MONTH_KEY.format(monthStart), MONTH_LABEL.format(monthStart),
                        monthStart.atStartOfDay(zone).toInstant(), monthEnd.atStartOfDay(zone).toInstant()));
                current = current.plusMonths(1);
            }
            return windows;
        }

        List<BucketWindow> windows = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            LocalDate next = current.plusDays(1);
            windows.add(new BucketWindow(DAY_KEY.format(current), DAY_LABEL.format(current),
                    current.atStartOfDay(zone).toInstant(), next.atStartOfDay(zone).toInstant()));
            current = next;
        }
        return windows;
    }

    public LocalDate startOfWeek(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    private Set<String> normalizedProjects(List<String> projectNames) {
        if (projectNames == null) {
            return Set.of();
        }
        return projectNames.stream()
            .filter(project -> project != null && !project.isBlank())
            .map(project -> project.trim().toLowerCase(Locale.ROOT))
            .collect(Collectors.toSet());
    }

    private boolean taskMatches(TimeEntry entry, Set<UUID> selectedTasks, boolean includeNoTask) {
        if (entry.getTask() == null) {
            return includeNoTask;
        }
        return selectedTasks.contains(entry.getTask().getId());
    }

    private List<TimeEntry> visibleEntries(User user) {
        if (user.getActiveWorkspaceType() == WorkspaceType.ORGANIZATION) {
            OrganizationMember member = workspaceService.activeOrganizationMembership(user);
            List<TimeEntry> entries = timeEntryRepository.findByOrganizationIdOrderByStartedAtDesc(
                    member.getOrganization().getId());
            if (member.getRole() == OrganizationRole.MEMBER) {
                return entries.stream().filter(entry -> entry.getUser().getId().equals(user.getId())).toList();
            }
            return entries;
        }
        return timeEntryRepository.findByUserIdOrderByStartedAtDesc(user.getId()).stream()
            .filter(entry -> entry.getWorkspaceType() == null || entry.getWorkspaceType() == WorkspaceType.PERSONAL)
            .toList();
    }

    private String projectName(TimeEntry entry) {
        return entry.getProject() == null ? entry.getProjectName() : entry.getProject().getName();
    }

    private TaskSegmentKey taskSegmentKey(TimeEntry entry) {
        if (entry.getTask() == null) {
            return new TaskSegmentKey(null, null, null, projectName(entry));
        }
        return new TaskSegmentKey(entry.getTask().getId(), entry.getTask().getName(), projectName(entry),
                projectName(entry) + " / " + entry.getTask().getName());
    }

    private boolean overlaps(Instant start, Instant end, Instant rangeStart, Instant rangeEnd) {
        return start.isBefore(rangeEnd) && end.isAfter(rangeStart);
    }

    private long overlapSeconds(Instant start, Instant end, Instant rangeStart, Instant rangeEnd) {
        Instant clippedStart = start.isAfter(rangeStart) ? start : rangeStart;
        Instant clippedEnd = end.isBefore(rangeEnd) ? end : rangeEnd;
        return Math.max(0, Duration.between(clippedStart, clippedEnd).toSeconds());
    }

    private BigDecimal amount(BigDecimal hourlyRate, long seconds) {
        return hourlyRate.multiply(BigDecimal.valueOf(seconds))
            .divide(BigDecimal.valueOf(3600), 2, RoundingMode.HALF_UP);
    }

    private Instant endInstant(TimeEntry entry, OffsetDateTime now) {
        return (entry.getEndedAt() == null ? now : entry.getEndedAt()).toInstant();
    }

    private OffsetDateTime now() {
        return OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);
    }

    private ZoneId zone(String timezone) {
        try {
            return ZoneId.of(timezone);
        } catch (RuntimeException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid timezone");
        }
    }

    private static final class Totals {
        private long seconds;
        private BigDecimal amount = BigDecimal.ZERO.setScale(2);

        private void add(long seconds, BigDecimal amount) {
            this.seconds += seconds;
            this.amount = this.amount.add(amount).setScale(2, RoundingMode.HALF_UP);
        }
    }

    private static final class ProjectTotals {
        private final String projectName;
        private long seconds;
        private BigDecimal amount = BigDecimal.ZERO.setScale(2);

        private ProjectTotals(String projectName) {
            this.projectName = projectName;
        }

        private void add(long seconds, BigDecimal amount) {
            this.seconds += seconds;
            this.amount = this.amount.add(amount).setScale(2, RoundingMode.HALF_UP);
        }

        private String projectName() {
            return projectName;
        }

        private long seconds() {
            return seconds;
        }
    }

    private record BucketWindow(String key, String label, Instant start, Instant end) {
    }
}
