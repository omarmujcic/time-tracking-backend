package com.omarmujcic.timetracking.core.timetracking;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.omarmujcic.timetracking.core.auth.entity.User;
import com.omarmujcic.timetracking.core.projects.ProjectService;
import com.omarmujcic.timetracking.core.projects.entity.Project;
import com.omarmujcic.timetracking.core.projects.entity.ProjectStatus;
import com.omarmujcic.timetracking.core.projects.entity.Task;
import com.omarmujcic.timetracking.core.projects.entity.TaskStatus;
import com.omarmujcic.timetracking.core.timetracking.dto.CreateTimeEntryRequestDTO;
import com.omarmujcic.timetracking.core.timetracking.dto.StartTimerRequestDTO;
import com.omarmujcic.timetracking.core.timetracking.dto.TimeEntryResponseDTO;
import com.omarmujcic.timetracking.core.timetracking.dto.TimeEntrySummaryDTO;
import com.omarmujcic.timetracking.core.timetracking.dto.UpdateTimeEntryRequestDTO;
import com.omarmujcic.timetracking.core.timetracking.entity.TimeEntry;
import com.omarmujcic.timetracking.core.timetracking.mapper.TimeEntryMapper;
import com.omarmujcic.timetracking.core.timetracking.repository.TimeEntryRepository;
import com.omarmujcic.timetracking.core.workspace.WorkspaceService;
import com.omarmujcic.timetracking.core.workspace.entity.Organization;
import com.omarmujcic.timetracking.core.workspace.entity.OrganizationMember;
import com.omarmujcic.timetracking.core.workspace.entity.OrganizationRole;
import com.omarmujcic.timetracking.core.workspace.entity.WorkspaceType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TimeEntryService {

    private final TimeEntryRepository timeEntryRepository;
    private final TimeEntryMapper timeEntryMapper;
    private final ProjectService projectService;
    private final WorkspaceService workspaceService;

    @Transactional(readOnly = true)
    public List<TimeEntryResponseDTO> list(User user, YearMonth month, LocalDate day, String project, UUID userId,
            List<String> projectNames, List<UUID> userIds) {
        OffsetDateTime now = now();
        return timeEntryMapper.toResponseDTOs(filteredEntries(user, month, day, project, userId, projectNames, userIds,
                now), now);
    }

    @Transactional(readOnly = true)
    public TimeEntrySummaryDTO summary(User user, YearMonth month, LocalDate day, String project, UUID userId,
            List<String> projectNames, List<UUID> userIds) {
        OffsetDateTime now = now();
        return timeEntryMapper.toSummaryDTO(filteredEntries(user, month, day, project, userId, projectNames, userIds,
                now), now);
    }

    @Transactional
    public TimeEntryResponseDTO start(User user, StartTimerRequestDTO request) {
        OffsetDateTime now = now();
        if (timeEntryRepository.findByUserIdAndEndedAtIsNull(user.getId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An active timer is already running");
        }

        EntryWorkspace workspace = entryWorkspace(user);
        Project project = request.getProjectId() == null ? null : resolveProject(user, request.getProjectId());
        Task task = project == null ? null : resolveTask(user, project, request.getTaskId());
        TimeEntry entry = timeEntryMapper.toTimerEntity(request, user, workspace.type(), workspace.organization(), project,
                task, now);
        assertNoOverlap(user, entry.getStartedAt(), null, null, now);
        return timeEntryMapper.toResponseDTO(timeEntryRepository.save(entry), now);
    }

    @Transactional
    public TimeEntryResponseDTO stop(User user, UUID id) {
        OffsetDateTime now = now();
        TimeEntry entry = findOwnedEntry(user, id);
        if (entry.getEndedAt() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Timer is already stopped");
        }
        if (!entry.getStartedAt().isBefore(now)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Timer cannot end before it starts");
        }
        if (entry.getProject() == null || entry.getHourlyRate().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Project and hourly rate are required before stopping timer");
        }

        timeEntryMapper.stopTimer(now, entry);
        assertNoOverlap(user, entry.getStartedAt(), entry.getEndedAt(), entry.getId(), now);
        return timeEntryMapper.toResponseDTO(entry, now);
    }

    @Transactional
    public TimeEntryResponseDTO create(User user, CreateTimeEntryRequestDTO request) {
        OffsetDateTime now = now();
        OffsetDateTime startedAt = timeEntryMapper.truncateToSeconds(request.getStartedAt());
        OffsetDateTime endedAt = timeEntryMapper.truncateToSeconds(request.getEndedAt());
        assertValidCompletedRange(startedAt, endedAt);
        assertNoOverlap(user, startedAt, endedAt, null, now);

        EntryWorkspace workspace = entryWorkspace(user);
        Project project = resolveProject(user, request.getProjectId());
        Task task = resolveTask(user, project, request.getTaskId());
        TimeEntry entry = timeEntryMapper.toManualEntity(request, user, workspace.type(), workspace.organization(), project,
                task, startedAt, endedAt, now);
        return timeEntryMapper.toResponseDTO(timeEntryRepository.save(entry), now);
    }

    @Transactional
    public TimeEntryResponseDTO update(User user, UUID id, UpdateTimeEntryRequestDTO request) {
        OffsetDateTime now = now();
        TimeEntry entry = findOwnedEntry(user, id);
        OffsetDateTime startedAt = timeEntryMapper.truncateToSeconds(request.getStartedAt());
        OffsetDateTime endedAt = timeEntryMapper.truncateToSeconds(request.getEndedAt());

        if (endedAt != null) {
            assertValidCompletedRange(startedAt, endedAt);
        } else if (timeEntryRepository.findByUserIdAndEndedAtIsNull(user.getId())
                .filter(activeEntry -> !activeEntry.getId().equals(id))
                .isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An active timer is already running");
        }

        Project project = resolveProject(user, request.getProjectId());
        Task task = resolveTask(user, project, request.getTaskId());
        assertNoOverlap(user, startedAt, endedAt, id, now);
        timeEntryMapper.updateEntity(request, project, task, startedAt, endedAt, now, entry);
        return timeEntryMapper.toResponseDTO(entry, now);
    }

    @Transactional
    public void delete(User user, UUID id) {
        timeEntryRepository.delete(findOwnedEntry(user, id));
    }

    private TimeEntry findOwnedEntry(User user, UUID id) {
        return timeEntryRepository.findByIdAndUserId(id, user.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Time entry not found"));
    }

    private List<TimeEntry> filteredEntries(User user, YearMonth month, LocalDate day, String project, UUID userId,
            List<String> projectNames, List<UUID> userIds, OffsetDateTime now) {
        Set<UUID> requestedUserIds = new HashSet<>();
        if (userIds != null) {
            requestedUserIds.addAll(userIds);
        }
        if (userId != null) {
            requestedUserIds.add(userId);
        }
        if (!requestedUserIds.isEmpty() && !requestedUserIds.equals(Set.of(user.getId())) && !canViewOrganizationEntries(user)) {
            return List.of();
        }

        String normalizedProject = project == null ? null : project.trim().toLowerCase();
        Set<String> requestedProjectNames = new HashSet<>();
        if (projectNames != null) {
            projectNames.stream()
                .map(String::trim)
                .filter(name -> !name.isBlank())
                .map(name -> name.toLowerCase(Locale.ROOT))
                .forEach(requestedProjectNames::add);
        }
        return visibleEntries(user).stream()
            .filter(entry -> requestedUserIds.isEmpty() || requestedUserIds.contains(entry.getUser().getId()))
            .filter(entry -> isWithinDateFilters(entry, month, day, now))
            .filter(entry -> normalizedProject == null || normalizedProject.isBlank()
                || timeEntryMapper.projectName(entry).toLowerCase().contains(normalizedProject))
            .filter(entry -> requestedProjectNames.isEmpty()
                || requestedProjectNames.contains(timeEntryMapper.projectName(entry).toLowerCase(Locale.ROOT)))
            .toList();
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

    private boolean canViewOrganizationEntries(User user) {
        if (user.getActiveWorkspaceType() != WorkspaceType.ORGANIZATION) {
            return false;
        }
        OrganizationRole role = workspaceService.activeOrganizationMembership(user).getRole();
        return role == OrganizationRole.OWNER || role == OrganizationRole.ADMIN;
    }

    private Project resolveProject(User user, UUID projectId) {
        if (projectId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Project is required");
        }
        Project project = projectService.findAccessibleProject(user, projectId);
        if (project.getStatus() != ProjectStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Project is inactive");
        }
        return project;
    }

    private Task resolveTask(User user, Project project, UUID taskId) {
        if (taskId == null) {
            return null;
        }
        Task task = projectService.findAccessibleTask(user, project.getId(), taskId);
        if (task.getStatus() != TaskStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task is inactive");
        }
        return task;
    }

    private EntryWorkspace entryWorkspace(User user) {
        if (user.getActiveWorkspaceType() == WorkspaceType.ORGANIZATION) {
            OrganizationMember member = workspaceService.activeOrganizationMembership(user);
            return new EntryWorkspace(WorkspaceType.ORGANIZATION, member.getOrganization());
        }
        return new EntryWorkspace(WorkspaceType.PERSONAL, null);
    }

    private boolean isWithinDateFilters(TimeEntry entry, YearMonth month, LocalDate day, OffsetDateTime now) {
        LocalDate entryDay = entry.getStartedAt().withOffsetSameInstant(ZoneOffset.UTC).toLocalDate();
        if (day != null) {
            return entryDay.equals(day);
        }
        if (month != null) {
            return YearMonth.from(entryDay).equals(month);
        }
        return true;
    }

    private void assertValidCompletedRange(OffsetDateTime startedAt, OffsetDateTime endedAt) {
        if (!startedAt.isBefore(endedAt)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End time must be after start time");
        }
    }

    private void assertNoOverlap(User user, OffsetDateTime startedAt, OffsetDateTime endedAt, UUID ignoredId,
            OffsetDateTime now) {
        boolean hasOverlap = timeEntryRepository.findByUserIdOrderByStartedAtDesc(user.getId()).stream()
            .filter(entry -> ignoredId == null || !entry.getId().equals(ignoredId))
            .anyMatch(entry -> overlaps(startedAt, endedAt, entry.getStartedAt(), entry.getEndedAt(), now));

        if (hasOverlap) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Time entry overlaps an existing entry");
        }
    }

    private boolean overlaps(OffsetDateTime startedAt, OffsetDateTime endedAt, OffsetDateTime existingStartedAt,
            OffsetDateTime existingEndedAt, OffsetDateTime now) {
        OffsetDateTime newEnd = endedAt == null ? OffsetDateTime.MAX : endedAt;
        OffsetDateTime existingEnd = existingEndedAt == null ? OffsetDateTime.MAX : existingEndedAt;
        return startedAt.isBefore(existingEnd) && existingStartedAt.isBefore(newEnd);
    }

    private OffsetDateTime now() {
        return OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
    }

    private record EntryWorkspace(WorkspaceType type, Organization organization) {
    }
}
