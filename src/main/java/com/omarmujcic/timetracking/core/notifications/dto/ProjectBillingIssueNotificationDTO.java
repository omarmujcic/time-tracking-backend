package com.omarmujcic.timetracking.core.notifications.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.omarmujcic.timetracking.core.auth.entity.User;
import com.omarmujcic.timetracking.core.workspace.entity.Organization;

public record ProjectBillingIssueNotificationDTO(
        String message,
        Organization organization,
        User creator,
        UUID projectId,
        String projectName,
        OffsetDateTime createdAt
) {
}
