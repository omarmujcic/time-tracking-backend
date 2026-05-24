package com.omarmujcic.timetracking.core.calendar.mapper;

import java.math.BigDecimal;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.omarmujcic.timetracking.core.calendar.dto.CalendarEntryDTO;
import com.omarmujcic.timetracking.core.timetracking.entity.TimeEntry;

@Mapper(componentModel = "spring")
public interface CalendarMapper {

    @Mapping(target = "id", source = "entry.id")
    @Mapping(target = "userId", source = "entry.user.id")
    @Mapping(target = "username", source = "entry.user.username")
    @Mapping(target = "displayName", source = "entry.user.displayName")
    @Mapping(target = "projectId", source = "entry.project.id")
    @Mapping(target = "projectName", expression = "java(projectName(entry))")
    @Mapping(target = "taskId", source = "entry.task.id")
    @Mapping(target = "taskName", source = "entry.task.name")
    @Mapping(target = "hourlyRate", source = "entry.hourlyRate")
    @Mapping(target = "currency", source = "entry.currency")
    @Mapping(target = "startedAt", source = "entry.startedAt")
    @Mapping(target = "endedAt", source = "entry.endedAt")
    @Mapping(target = "durationSeconds", source = "durationSeconds")
    @Mapping(target = "billableAmount", source = "billableAmount")
    @Mapping(target = "active", source = "active")
    CalendarEntryDTO toEntryDTO(TimeEntry entry, long durationSeconds, BigDecimal billableAmount, boolean active);

    default String projectName(TimeEntry entry) {
        return entry.getProject() == null ? entry.getProjectName() : entry.getProject().getName();
    }
}
