package com.omarmujcic.timetracking.core.tickettrackz.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.omarmujcic.timetracking.core.tickettrackz.entity.TicketPriority;
import com.omarmujcic.timetracking.core.tickettrackz.entity.TicketStatus;
import com.omarmujcic.timetracking.core.workspace.entity.WorkspaceType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TicketDTO {

    private UUID id;
    private String key;
    private WorkspaceType workspaceType;
    private UUID projectId;
    private String projectName;
    private UUID assigneeUserId;
    private String assigneeName;
    private UUID createdByUserId;
    private String createdByName;
    private String title;
    private String description;
    private String productContext;
    private TicketStatus status;
    private TicketPriority priority;
    private LocalDate dueDate;
    private Integer storyPoints;
    private BigDecimal estimatedHours;
    private BigDecimal actualHours;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
