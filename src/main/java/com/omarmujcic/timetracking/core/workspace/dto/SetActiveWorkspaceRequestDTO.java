package com.omarmujcic.timetracking.core.workspace.dto;

import java.util.UUID;

import com.omarmujcic.timetracking.core.workspace.entity.WorkspaceType;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SetActiveWorkspaceRequestDTO {
    @NotNull
    private WorkspaceType type;
    private UUID organizationId;
}
