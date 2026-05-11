package com.omarmujcic.timetracking.core.reports.mapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.omarmujcic.timetracking.core.reports.dto.ReportEntryDTO;
import com.omarmujcic.timetracking.core.timetracking.entity.TimeEntry;

@Mapper(componentModel = "spring", imports = {DateTimeFormatter.class, Locale.class})
public interface ReportMapper {

    DateTimeFormatter DAY_KEY = DateTimeFormatter.ISO_LOCAL_DATE;
    DateTimeFormatter DAY_LABEL = DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH);

    @Mapping(target = "id", source = "entry.id")
    @Mapping(target = "userId", source = "entry.user.id")
    @Mapping(target = "username", source = "entry.user.username")
    @Mapping(target = "displayName", source = "entry.user.displayName")
    @Mapping(target = "projectName", expression = "java(entry.getProject() == null ? entry.getProjectName() : entry.getProject().getName())")
    @Mapping(target = "taskId", source = "entry.task.id")
    @Mapping(target = "taskName", source = "entry.task.name")
    @Mapping(target = "hourlyRate", source = "entry.hourlyRate")
    @Mapping(target = "currency", source = "entry.currency")
    @Mapping(target = "startedAt", source = "entry.startedAt")
    @Mapping(target = "endedAt", source = "entry.endedAt")
    @Mapping(target = "durationSeconds", source = "durationSeconds")
    @Mapping(target = "billableAmount", source = "billableAmount")
    @Mapping(target = "active", source = "active")
    @Mapping(target = "groupKey", expression = "java(DAY_KEY.format(entryDate))")
    @Mapping(target = "groupLabel", expression = "java(DAY_LABEL.format(entryDate))")
    ReportEntryDTO toEntryDTO(
            TimeEntry entry,
            long durationSeconds,
            BigDecimal billableAmount,
            boolean active,
            LocalDate entryDate
    );
}
