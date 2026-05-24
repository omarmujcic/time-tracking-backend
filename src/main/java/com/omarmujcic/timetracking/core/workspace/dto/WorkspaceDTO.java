package com.omarmujcic.timetracking.core.workspace.dto;

import java.util.UUID;

import com.omarmujcic.timetracking.core.workspace.entity.OrganizationRole;
import com.omarmujcic.timetracking.core.workspace.entity.WorkspaceType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkspaceDTO {
    private WorkspaceType type;
    private UUID organizationId;
    private String name;
    private String joinCode;
    private OrganizationRole role;
    private boolean membersCanCreateTasks;
    private boolean active;
}
