package com.omarmujcic.timetracking.core.workspace.dto;

import com.omarmujcic.timetracking.core.workspace.entity.OrganizationRole;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateOrganizationMemberRoleRequestDTO {

    @NotNull
    private OrganizationRole role;
}
