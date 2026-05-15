package com.omarmujcic.timetracking.core.projects.mapper;

import java.util.List;
import java.util.UUID;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.omarmujcic.timetracking.core.auth.entity.User;
import com.omarmujcic.timetracking.core.projects.dto.ProjectDTO;
import com.omarmujcic.timetracking.core.projects.dto.ProjectBillingRuleDTO;
import com.omarmujcic.timetracking.core.projects.dto.TaskDTO;
import com.omarmujcic.timetracking.core.projects.dto.UpsertProjectRequestDTO;
import com.omarmujcic.timetracking.core.projects.dto.UpsertTaskRequestDTO;
import com.omarmujcic.timetracking.core.projects.entity.Project;
import com.omarmujcic.timetracking.core.projects.entity.Task;
import com.omarmujcic.timetracking.core.workspace.entity.Organization;

@Mapper(componentModel = "spring", imports = UUID.class)
public interface ProjectMapper {

    @Mapping(target = "id", source = "project.id")
    @Mapping(target = "name", source = "project.name")
    @Mapping(target = "status", source = "project.status")
    @Mapping(target = "hourlyRate", source = "project.hourlyRate")
    @Mapping(target = "currency", source = "project.currency")
    @Mapping(target = "billingRule", source = "billingRule")
    @Mapping(target = "tasks", source = "tasks")
    ProjectDTO toDTO(Project project, ProjectBillingRuleDTO billingRule, List<TaskDTO> tasks);

    @Mapping(target = "projectId", source = "project.id")
    TaskDTO toTaskDTO(Task task);

    @Mapping(target = "id", expression = "java(UUID.randomUUID())")
    @Mapping(target = "user", source = "user")
    @Mapping(target = "organization", source = "organization")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "status", source = "request.status")
    @Mapping(target = "hourlyRate", expression = "java(request.getHourlyRate().setScale(2, java.math.RoundingMode.HALF_UP))")
    @Mapping(target = "currency", source = "currency")
    @Mapping(target = "createdAt", source = "now")
    @Mapping(target = "updatedAt", source = "now")
    Project toEntity(UpsertProjectRequestDTO request, String name, User user, Organization organization, String currency,
            java.time.OffsetDateTime now);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "currency", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "name", source = "name")
    @Mapping(target = "status", source = "request.status")
    @Mapping(target = "hourlyRate", expression = "java(request.getHourlyRate().setScale(2, java.math.RoundingMode.HALF_UP))")
    @Mapping(target = "updatedAt", source = "now")
    void updateEntity(UpsertProjectRequestDTO request, String name, java.time.OffsetDateTime now,
            @MappingTarget Project project);

    @Mapping(target = "id", expression = "java(UUID.randomUUID())")
    @Mapping(target = "project", source = "project")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "status", source = "request.status")
    @Mapping(target = "createdAt", source = "now")
    @Mapping(target = "updatedAt", source = "now")
    Task toTaskEntity(UpsertTaskRequestDTO request, String name, Project project, java.time.OffsetDateTime now);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "name", source = "name")
    @Mapping(target = "status", source = "request.status")
    @Mapping(target = "updatedAt", source = "now")
    void updateTaskEntity(UpsertTaskRequestDTO request, String name, java.time.OffsetDateTime now,
            @MappingTarget Task task);
}
