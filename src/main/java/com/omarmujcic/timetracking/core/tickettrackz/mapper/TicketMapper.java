package com.omarmujcic.timetracking.core.tickettrackz.mapper;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.omarmujcic.timetracking.core.auth.entity.User;
import com.omarmujcic.timetracking.core.projects.entity.Project;
import com.omarmujcic.timetracking.core.tickettrackz.dto.CreateTicketRequestDTO;
import com.omarmujcic.timetracking.core.tickettrackz.dto.TicketDTO;
import com.omarmujcic.timetracking.core.tickettrackz.dto.UpdateTicketRequestDTO;
import com.omarmujcic.timetracking.core.tickettrackz.entity.Ticket;
import com.omarmujcic.timetracking.core.workspace.entity.Organization;
import com.omarmujcic.timetracking.core.workspace.entity.WorkspaceType;

@Mapper(componentModel = "spring", imports = {BigDecimal.class, UUID.class})
public interface TicketMapper {

    @Mapping(target = "key", source = "ticket.ticketKey")
    @Mapping(target = "projectId", source = "ticket.project.id")
    @Mapping(target = "projectName", source = "ticket.project.name")
    @Mapping(target = "assigneeUserId", source = "ticket.assignee.id")
    @Mapping(target = "assigneeName", source = "ticket.assignee.displayName")
    @Mapping(target = "createdByUserId", source = "ticket.createdBy.id")
    @Mapping(target = "createdByName", source = "ticket.createdBy.displayName")
    @Mapping(target = "actualHours", expression = "java(BigDecimal.ZERO.setScale(2))")
    TicketDTO toDTO(Ticket ticket);

    @Mapping(target = "id", expression = "java(UUID.randomUUID())")
    @Mapping(target = "ticketKey", source = "key")
    @Mapping(target = "workspaceType", source = "workspaceType")
    @Mapping(target = "workspaceUser", source = "workspaceUser")
    @Mapping(target = "organization", source = "organization")
    @Mapping(target = "project", source = "project")
    @Mapping(target = "assignee", source = "assignee")
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "title", expression = "java(TicketMapperSupport.trimRequired(request.getTitle()))")
    @Mapping(target = "description", expression = "java(TicketMapperSupport.trimToNull(request.getDescription()))")
    @Mapping(target = "productContext", expression = "java(TicketMapperSupport.trimToNull(request.getProductContext()))")
    @Mapping(target = "status", source = "request.status")
    @Mapping(target = "priority", source = "request.priority")
    @Mapping(target = "dueDate", source = "request.dueDate")
    @Mapping(target = "storyPoints", source = "request.storyPoints")
    @Mapping(target = "estimatedHours", expression = "java(TicketMapperSupport.scaleHours(request.getEstimatedHours()))")
    @Mapping(target = "createdAt", source = "now")
    @Mapping(target = "updatedAt", source = "now")
    Ticket toEntity(CreateTicketRequestDTO request, String key, WorkspaceType workspaceType, User workspaceUser,
            Organization organization, Project project, User assignee, User createdBy, OffsetDateTime now);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ticketKey", ignore = true)
    @Mapping(target = "workspaceType", ignore = true)
    @Mapping(target = "workspaceUser", ignore = true)
    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "project", source = "project")
    @Mapping(target = "assignee", source = "assignee")
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "title", expression = "java(TicketMapperSupport.trimRequired(request.getTitle()))")
    @Mapping(target = "description", expression = "java(TicketMapperSupport.trimToNull(request.getDescription()))")
    @Mapping(target = "productContext", expression = "java(TicketMapperSupport.trimToNull(request.getProductContext()))")
    @Mapping(target = "status", source = "request.status")
    @Mapping(target = "priority", source = "request.priority")
    @Mapping(target = "dueDate", source = "request.dueDate")
    @Mapping(target = "storyPoints", source = "request.storyPoints")
    @Mapping(target = "estimatedHours", expression = "java(TicketMapperSupport.scaleHours(request.getEstimatedHours()))")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", source = "now")
    void updateEntity(UpdateTicketRequestDTO request, Project project, User assignee, OffsetDateTime now,
            @MappingTarget Ticket ticket);
}
