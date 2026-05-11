package com.omarmujcic.timetracking.core.workspace.dto;

import java.util.UUID;

import com.omarmujcic.timetracking.core.workspace.entity.OrganizationRole;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrganizationDTO {
    private UUID id;
    private String name;
    private String joinCode;
    private OrganizationRole role;
}
