package com.omarmujcic.timetracking.core.timetracking.mapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import com.omarmujcic.timetracking.core.auth.entity.User;
import com.omarmujcic.timetracking.core.projects.entity.Project;
import com.omarmujcic.timetracking.core.projects.entity.Task;
import com.omarmujcic.timetracking.core.timetracking.dto.CreateTimeEntryRequestDTO;
import com.omarmujcic.timetracking.core.timetracking.dto.StartTimerRequestDTO;
import com.omarmujcic.timetracking.core.timetracking.dto.TimeEntryPageDTO;
import com.omarmujcic.timetracking.core.timetracking.dto.TimeEntryResponseDTO;
import com.omarmujcic.timetracking.core.timetracking.dto.TimeEntrySummaryDTO;
import com.omarmujcic.timetracking.core.timetracking.dto.UpdateTimeEntryRequestDTO;
import com.omarmujcic.timetracking.core.timetracking.entity.TimeEntry;
import com.omarmujcic.timetracking.core.workspace.entity.Organization;
import com.omarmujcic.timetracking.core.workspace.entity.WorkspaceType;

@Mapper(componentModel = "spring", imports = UUID.class)
public interface TimeEntryMapper {

    String DEFAULT_CURRENCY = "EUR";

    @Mapping(target = "id", expression = "java(UUID.randomUUID())")
    @Mapping(target = "user", source = "user")
    @Mapping(target = "workspaceType", source = "workspaceType")
    @Mapping(target = "organization", source = "organization")
    @Mapping(target = "project", source = "project")
    @Mapping(target = "task", source = "task")
    @Mapping(target = "projectName", source = "project.name")
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "hourlyRate", source = "request.hourlyRate", qualifiedByName = "normalizeRate")
    @Mapping(target = "currency", expression = "java(defaultCurrency())")
    @Mapping(target = "startedAt", source = "startedAt")
    @Mapping(target = "endedAt", source = "endedAt")
    @Mapping(target = "createdAt", source = "now")
    @Mapping(target = "updatedAt", source = "now")
    TimeEntry toManualEntity(CreateTimeEntryRequestDTO request, User user, WorkspaceType workspaceType,
            Organization organization, Project project, Task task, OffsetDateTime startedAt, OffsetDateTime endedAt,
            OffsetDateTime now);

    @Mapping(target = "id", expression = "java(UUID.randomUUID())")
    @Mapping(target = "user", source = "user")
    @Mapping(target = "workspaceType", source = "workspaceType")
    @Mapping(target = "organization", source = "organization")
    @Mapping(target = "project", source = "project")
    @Mapping(target = "task", source = "task")
    @Mapping(target = "projectName", expression = "java(timerProjectName(request, project))")
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "hourlyRate", source = "request.hourlyRate", qualifiedByName = "normalizeRate")
    @Mapping(target = "currency", expression = "java(defaultCurrency())")
    @Mapping(target = "startedAt", source = "now")
    @Mapping(target = "endedAt", ignore = true)
    @Mapping(target = "createdAt", source = "now")
    @Mapping(target = "updatedAt", source = "now")
    TimeEntry toTimerEntity(StartTimerRequestDTO request, User user, WorkspaceType workspaceType,
            Organization organization, Project project, Task task, OffsetDateTime now);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "workspaceType", ignore = true)
    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "project", source = "project")
    @Mapping(target = "task", source = "task")
    @Mapping(target = "currency", expression = "java(defaultCurrency())")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", source = "now")
    @Mapping(target = "projectName", source = "project.name")
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "hourlyRate", source = "request.hourlyRate", qualifiedByName = "normalizeRate")
    @Mapping(target = "startedAt", source = "startedAt")
    @Mapping(target = "endedAt", source = "endedAt")
    void updateEntity(UpdateTimeEntryRequestDTO request, Project project, Task task, OffsetDateTime startedAt,
            OffsetDateTime endedAt, OffsetDateTime now, @MappingTarget TimeEntry entry);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "endedAt", source = "endedAt")
    @Mapping(target = "updatedAt", source = "endedAt")
    void stopTimer(OffsetDateTime endedAt, @MappingTarget TimeEntry entry);

    @Mapping(target = "userId", source = "entry.user.id")
    @Mapping(target = "username", source = "entry.user.username")
    @Mapping(target = "displayName", source = "entry.user.displayName")
    @Mapping(target = "projectId", source = "entry.project.id")
    @Mapping(target = "projectName", expression = "java(projectName(entry))")
    @Mapping(target = "taskId", source = "entry.task.id")
    @Mapping(target = "taskName", source = "entry.task.name")
    @Mapping(target = "durationSeconds", expression = "java(durationSeconds(entry, now))")
    @Mapping(target = "billableAmount", expression = "java(billableAmount(entry, now))")
    @Mapping(target = "active", expression = "java(entry.getEndedAt() == null)")
    TimeEntryResponseDTO toResponseDTO(TimeEntry entry, OffsetDateTime now);

    default List<TimeEntryResponseDTO> toResponseDTOs(List<TimeEntry> entries, OffsetDateTime now) {
        return entries.stream()
            .map(entry -> toResponseDTO(entry, now))
            .toList();
    }

    default TimeEntrySummaryDTO toSummaryDTO(List<TimeEntry> entries, OffsetDateTime now) {
        return new TimeEntrySummaryDTO(
            entries.stream().mapToLong(entry -> durationSeconds(entry, now)).sum(),
            entries.stream()
                .map(entry -> billableAmount(entry, now))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP),
            DEFAULT_CURRENCY,
            entries.size(),
            entries.stream().anyMatch(entry -> entry.getEndedAt() == null)
        );
    }

    default TimeEntryPageDTO toPageDTO(List<TimeEntry> entries, int pageSize, boolean hasNext, boolean hasPrevious,
            String nextCursor, String previousCursor, OffsetDateTime now) {
        return new TimeEntryPageDTO(toResponseDTOs(entries, now), pageSize, hasNext, hasPrevious, nextCursor,
                previousCursor);
    }

    default OffsetDateTime truncateToSeconds(OffsetDateTime value) {
        return value == null ? null : value.truncatedTo(ChronoUnit.SECONDS);
    }

    default String defaultCurrency() {
        return DEFAULT_CURRENCY;
    }

    @Named("normalizeProjectName")
    default String normalizeProjectName(String projectName) {
        return projectName.trim();
    }

    default String projectName(TimeEntry entry) {
        return entry.getProject() == null ? entry.getProjectName() : entry.getProject().getName();
    }

    default String timerProjectName(StartTimerRequestDTO request, Project project) {
        if (project != null) {
            return project.getName();
        }
        if (request.getProjectName() != null && !request.getProjectName().isBlank()) {
            return request.getProjectName().trim();
        }
        return "Unassigned";
    }

    @Named("normalizeDescription")
    default String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }
        return description.trim();
    }

    @Named("normalizeRate")
    default BigDecimal normalizeRate(BigDecimal hourlyRate) {
        if (hourlyRate == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return hourlyRate.setScale(2, RoundingMode.HALF_UP);
    }

    default long durationSeconds(TimeEntry entry, OffsetDateTime now) {
        OffsetDateTime end = entry.getEndedAt() == null ? now : entry.getEndedAt();
        return Math.max(0, Duration.between(entry.getStartedAt(), end).toSeconds());
    }

    default BigDecimal billableAmount(TimeEntry entry, OffsetDateTime now) {
        BigDecimal hours = BigDecimal.valueOf(durationSeconds(entry, now))
            .divide(BigDecimal.valueOf(3600), 6, RoundingMode.HALF_UP);
        return entry.getHourlyRate().multiply(hours).setScale(2, RoundingMode.HALF_UP);
    }
}
