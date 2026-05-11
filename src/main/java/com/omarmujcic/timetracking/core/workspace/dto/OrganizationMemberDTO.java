package com.omarmujcic.timetracking.core.workspace.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.omarmujcic.timetracking.core.workspace.entity.OrganizationRole;

import lombok.Data;

@Data
public class OrganizationMemberDTO {

    private UUID userId;
    private String username;
    private String displayName;
    private OrganizationRole role;
    private OffsetDateTime joinedAt;
}
