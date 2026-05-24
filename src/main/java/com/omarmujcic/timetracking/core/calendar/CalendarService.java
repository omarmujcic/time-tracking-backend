package com.omarmujcic.timetracking.core.calendar;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.omarmujcic.timetracking.core.auth.entity.User;
import com.omarmujcic.timetracking.core.calendar.dto.CalendarDayDTO;
import com.omarmujcic.timetracking.core.calendar.dto.CalendarDayDetailDTO;
import com.omarmujcic.timetracking.core.calendar.dto.CalendarEntryDTO;
import com.omarmujcic.timetracking.core.calendar.dto.CalendarMonthDTO;
import com.omarmujcic.timetracking.core.calendar.dto.CalendarProjectTotalDTO;
import com.omarmujcic.timetracking.core.calendar.dto.CalendarSummaryDTO;
import com.omarmujcic.timetracking.core.calendar.dto.CalendarUserTotalDTO;
import com.omarmujcic.timetracking.core.calendar.dto.CalendarWeekDTO;
import com.omarmujcic.timetracking.core.calendar.mapper.CalendarMapper;
import com.omarmujcic.timetracking.core.timetracking.entity.TimeEntry;
import com.omarmujcic.timetracking.core.timetracking.repository.TimeEntryRepository;
import com.omarmujcic.timetracking.core.workspace.WorkspaceService;
import com.omarmujcic.timetracking.core.workspace.entity.OrganizationMember;
import com.omarmujcic.timetracking.core.workspace.entity.OrganizationRole;
import com.omarmujcic.timetracking.core.workspace.entity.WorkspaceType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private static final String CURRENCY = "EUR";

    private final TimeEntryRepository timeEntryRepository;
    private final WorkspaceService workspaceService;
    private final CalendarMapper calendarMapper;

    @Transactional(readOnly = true)
    public CalendarMonthDTO month(User user, YearMonth month, String timezone, boolean includeOrganizationEntries) {
        LocalDate startDate = month.atDay(1);
        LocalDate endDate = month.atEndOfMonth();
        CalendarPeriod period = period(user, startDate, endDate, timezone, includeOrganizationEntries);
        return new CalendarMonthDTO(period.viewerMode(), period.summary(), period.days());
    }

    @Transactional(readOnly = true)
    public CalendarWeekDTO week(User user, LocalDate date, String timezone, boolean includeOrganizationEntries) {
        LocalDate startDate = date.minusDays(date.getDayOfWeek().getValue() - 1L);
        LocalDate endDate = startDate.plusDays(6);
        CalendarPeriod period = period(user, startDate, endDate, timezone, includeOrganizationEntries);
        return new CalendarWeekDTO(period.viewerMode(), startDate, endDate, period.summary(), period.days(),
                period.entries());
    }

    @Transactional(readOnly = true)
    public CalendarDayDetailDTO day(User user, LocalDate date, String timezone, boolean includeOrganizationEntries) {
        ZoneId zone = zone(timezone);
        OffsetDateTime now = now();
        CalendarContext context = calendarContext(user, includeOrganizationEntries);
        Instant rangeStart = date.atStartOfDay(zone).toInstant();
        Instant rangeEnd = date.plusDays(1).atStartOfDay(zone).toInstant();
        DayTotals dayTotals = new DayTotals(date);
        List<CalendarEntryDTO> calendarEntries = new ArrayList<>();

        List<TimeEntry> entries = context.entries().stream()
            .filter(entry -> overlaps(entry.getStartedAt().toInstant(), endInstant(entry, now), rangeStart, rangeEnd))
            .toList();

        for (TimeEntry entry : entries) {
            long seconds = overlapSeconds(entry.getStartedAt().toInstant(), endInstant(entry, now), rangeStart, rangeEnd);
            if (seconds <= 0) {
                continue;
            }
            BigDecimal entryAmount = amount(entry.getHourlyRate(), seconds);
            boolean active = entry.getEndedAt() == null;
            dayTotals.add(entry, seconds, entryAmount, active);
            calendarEntries.add(calendarMapper.toEntryDTO(entry, seconds, entryAmount, active));
        }

        calendarEntries.sort((first, second) -> {
            int activeComparison = Boolean.compare(second.active(), first.active());
            if (activeComparison != 0) {
                return activeComparison;
            }
            return second.startedAt().compareTo(first.startedAt());
        });

        CalendarSummaryDTO summary = new CalendarSummaryDTO(
                dayTotals.seconds,
                dayTotals.amount,
                dayTotals.seconds > 0 ? 1 : 0,
                dayTotals.entryIds.size(),
                dayTotals.activeSeconds,
                dayTotals.activeEntryIds.size(),
                CURRENCY
        );
        return new CalendarDayDetailDTO(context.viewerMode(), date, summary, projectTotals(dayTotals.projectTotals),
                userTotals(dayTotals.userTotals), calendarEntries);
    }

    private void addEntryToDays(TimeEntry entry, OffsetDateTime now, ZoneId zone, LocalDate rangeStartDate,
            LocalDate rangeEndDate, Map<LocalDate, DayTotals> dayTotals) {
        Instant entryStart = entry.getStartedAt().toInstant();
        Instant entryEnd = endInstant(entry, now);
        LocalDate entryStartDate = entryStart.atZone(zone).toLocalDate();
        LocalDate entryEndDate = entryEnd.minusNanos(1).atZone(zone).toLocalDate();
        LocalDate current = max(entryStartDate, rangeStartDate);
        LocalDate last = min(entryEndDate, rangeEndDate);

        while (!current.isAfter(last)) {
            Instant dayStart = current.atStartOfDay(zone).toInstant();
            Instant dayEnd = current.plusDays(1).atStartOfDay(zone).toInstant();
            long seconds = overlapSeconds(entryStart, entryEnd, dayStart, dayEnd);
            if (seconds > 0) {
                boolean active = entry.getEndedAt() == null
                    && current.equals(now.atZoneSameInstant(zone).toLocalDate());
                dayTotals.get(current).add(entry, seconds, amount(entry.getHourlyRate(), seconds), active);
            }
            current = current.plusDays(1);
        }
    }

    private CalendarPeriod period(User user, LocalDate startDate, LocalDate endDate, String timezone,
            boolean includeOrganizationEntries) {
        ZoneId zone = zone(timezone);
        OffsetDateTime now = now();
        CalendarContext context = calendarContext(user, includeOrganizationEntries);
        Instant rangeStart = startDate.atStartOfDay(zone).toInstant();
        Instant rangeEnd = endDate.plusDays(1).atStartOfDay(zone).toInstant();

        Map<LocalDate, DayTotals> dayTotals = new LinkedHashMap<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            dayTotals.put(current, new DayTotals(current));
            current = current.plusDays(1);
        }

        SummaryAccumulator summary = new SummaryAccumulator();
        List<TimeEntry> entries = context.entries().stream()
            .filter(entry -> overlaps(entry.getStartedAt().toInstant(), endInstant(entry, now), rangeStart, rangeEnd))
            .toList();
        summary.addEntries(entries, now, rangeStart, rangeEnd);

        for (TimeEntry entry : entries) {
            addEntryToDays(entry, now, zone, startDate, endDate, dayTotals);
        }

        List<CalendarDayDTO> days = dayTotals.values().stream()
            .map(day -> new CalendarDayDTO(day.date, day.seconds, day.entryIds.size(), day.active,
                    projectTotals(day.projectTotals), userTotals(day.userTotals)))
            .toList();

        List<CalendarEntryDTO> calendarEntries = entries.stream()
            .map(entry -> {
                long seconds = overlapSeconds(entry.getStartedAt().toInstant(), endInstant(entry, now), rangeStart,
                        rangeEnd);
                return calendarMapper.toEntryDTO(entry, seconds, amount(entry.getHourlyRate(), seconds),
                        entry.getEndedAt() == null);
            })
            .sorted((first, second) -> first.startedAt().compareTo(second.startedAt()))
            .toList();

        return new CalendarPeriod(context.viewerMode(), summary.toSummaryDTO(workedDayCount(dayTotals)), days,
                calendarEntries);
    }

    private CalendarContext calendarContext(User user, boolean includeOrganizationEntries) {
        if (user.getActiveWorkspaceType() == WorkspaceType.ORGANIZATION) {
            OrganizationMember member = workspaceService.activeOrganizationMembership(user);
            List<TimeEntry> entries = timeEntryRepository.findByOrganizationIdOrderByStartedAtDesc(
                    member.getOrganization().getId());
            if (member.getRole() == OrganizationRole.MEMBER) {
                entries = entries.stream().filter(entry -> entry.getUser().getId().equals(user.getId())).toList();
                return new CalendarContext(CalendarViewerMode.PROJECT_BREAKDOWN, entries);
            }
            return new CalendarContext(CalendarViewerMode.USER_BREAKDOWN, entries);
        }

        List<TimeEntry> entries = timeEntryRepository.findByUserIdOrderByStartedAtDesc(user.getId());
        if (!includeOrganizationEntries) {
            entries = entries.stream()
                .filter(entry -> entry.getWorkspaceType() == null || entry.getWorkspaceType() == WorkspaceType.PERSONAL)
                .toList();
        }
        return new CalendarContext(CalendarViewerMode.PROJECT_BREAKDOWN, entries);
    }

    private int workedDayCount(Map<LocalDate, DayTotals> dayTotals) {
        return (int) dayTotals.values().stream().filter(day -> day.seconds > 0).count();
    }

    private List<CalendarProjectTotalDTO> projectTotals(Map<String, ProjectTotals> projectTotals) {
        return projectTotals.values().stream()
            .sorted(Comparator.comparingLong(ProjectTotals::seconds).reversed()
                .thenComparing(ProjectTotals::projectName, String.CASE_INSENSITIVE_ORDER))
            .map(project -> new CalendarProjectTotalDTO(project.projectName(), project.seconds(), project.amount()))
            .toList();
    }

    private List<CalendarUserTotalDTO> userTotals(Map<UUID, UserTotals> userTotals) {
        return userTotals.values().stream()
            .sorted(Comparator.comparingLong(UserTotals::seconds).reversed()
                .thenComparing(UserTotals::displayName, String.CASE_INSENSITIVE_ORDER))
            .map(user -> new CalendarUserTotalDTO(user.userId(), user.username(), user.displayName(), user.seconds(),
                    user.amount()))
            .toList();
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

    private String projectName(TimeEntry entry) {
        return entry.getProject() == null ? entry.getProjectName() : entry.getProject().getName();
    }

    private Instant endInstant(TimeEntry entry, OffsetDateTime now) {
        return (entry.getEndedAt() == null ? now : entry.getEndedAt()).toInstant();
    }

    private LocalDate min(LocalDate first, LocalDate second) {
        return first.isBefore(second) ? first : second;
    }

    private LocalDate max(LocalDate first, LocalDate second) {
        return first.isAfter(second) ? first : second;
    }

    private OffsetDateTime now() {
        return OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);
    }

    private ZoneId zone(String timezone) {
        try {
            return ZoneId.of(timezone == null || timezone.isBlank() ? "UTC" : timezone);
        } catch (RuntimeException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid timezone");
        }
    }

    private record CalendarContext(CalendarViewerMode viewerMode, List<TimeEntry> entries) {
    }

    private record CalendarPeriod(CalendarViewerMode viewerMode, CalendarSummaryDTO summary, List<CalendarDayDTO> days,
            List<CalendarEntryDTO> entries) {
    }

    private final class SummaryAccumulator {
        private long seconds;
        private BigDecimal amount = BigDecimal.ZERO.setScale(2);
        private long activeSeconds;
        private int entryCount;
        private int activeEntryCount;

        private void addEntries(List<TimeEntry> entries, OffsetDateTime now, Instant rangeStart, Instant rangeEnd) {
            entryCount = entries.size();
            activeEntryCount = (int) entries.stream().filter(entry -> entry.getEndedAt() == null).count();
            for (TimeEntry entry : entries) {
                long entrySeconds = overlapSeconds(entry.getStartedAt().toInstant(), endInstant(entry, now),
                        rangeStart, rangeEnd);
                BigDecimal entryAmount = amount(entry.getHourlyRate(), entrySeconds);
                seconds += entrySeconds;
                amount = amount.add(entryAmount).setScale(2, RoundingMode.HALF_UP);
                if (entry.getEndedAt() == null) {
                    activeSeconds += entrySeconds;
                }
            }
        }

        private CalendarSummaryDTO toSummaryDTO(int workedDayCount) {
            return new CalendarSummaryDTO(seconds, amount, workedDayCount, entryCount, activeSeconds, activeEntryCount,
                    CURRENCY);
        }
    }

    private final class DayTotals {
        private final LocalDate date;
        private long seconds;
        private BigDecimal amount = BigDecimal.ZERO.setScale(2);
        private boolean active;
        private long activeSeconds;
        private final LinkedHashSet<UUID> entryIds = new LinkedHashSet<>();
        private final LinkedHashSet<UUID> activeEntryIds = new LinkedHashSet<>();
        private final Map<String, ProjectTotals> projectTotals = new LinkedHashMap<>();
        private final Map<UUID, UserTotals> userTotals = new LinkedHashMap<>();

        private DayTotals(LocalDate date) {
            this.date = date;
        }

        private void add(TimeEntry entry, long seconds, BigDecimal amount, boolean active) {
            this.seconds += seconds;
            this.amount = this.amount.add(amount).setScale(2, RoundingMode.HALF_UP);
            this.active = this.active || active;
            this.entryIds.add(entry.getId());
            if (active) {
                this.activeSeconds += seconds;
                this.activeEntryIds.add(entry.getId());
            }
            projectTotals.computeIfAbsent(projectName(entry), ProjectTotals::new).add(seconds, amount);
            userTotals.computeIfAbsent(entry.getUser().getId(), ignored -> new UserTotals(entry.getUser().getId(),
                    entry.getUser().getUsername(), entry.getUser().getDisplayName())).add(seconds, amount);
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

        private BigDecimal amount() {
            return amount;
        }
    }

    private static final class UserTotals {
        private final UUID userId;
        private final String username;
        private final String displayName;
        private long seconds;
        private BigDecimal amount = BigDecimal.ZERO.setScale(2);

        private UserTotals(UUID userId, String username, String displayName) {
            this.userId = userId;
            this.username = username;
            this.displayName = displayName;
        }

        private void add(long seconds, BigDecimal amount) {
            this.seconds += seconds;
            this.amount = this.amount.add(amount).setScale(2, RoundingMode.HALF_UP);
        }

        private UUID userId() {
            return userId;
        }

        private String username() {
            return username;
        }

        private String displayName() {
            return displayName;
        }

        private long seconds() {
            return seconds;
        }

        private BigDecimal amount() {
            return amount;
        }
    }
}
