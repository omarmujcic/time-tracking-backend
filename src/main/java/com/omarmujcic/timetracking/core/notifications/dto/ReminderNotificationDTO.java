package com.omarmujcic.timetracking.core.notifications.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.omarmujcic.timetracking.core.auth.entity.User;
import com.omarmujcic.timetracking.core.notifications.entity.NotificationType;
import com.omarmujcic.timetracking.core.workspace.entity.Organization;
import com.omarmujcic.timetracking.core.workspace.entity.WorkspaceType;

public record ReminderNotificationDTO(
        WorkspaceType workspaceType,
        User workspaceUser,
        Organization organization,
        User recipientUser,
        NotificationType type,
        String message,
        String subjectType,
        UUID subjectId,
        String subjectLabel,
        String sourceRoute,
        String sourceLabel,
        String reminderKey,
        OffsetDateTime createdAt,
        OffsetDateTime emailEscalationDueAt
) {
}
