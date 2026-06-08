package com.omarmujcic.timetracking.core.notifications.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.omarmujcic.timetracking.core.notifications.entity.NotificationStatus;
import com.omarmujcic.timetracking.core.notifications.entity.NotificationType;
import com.omarmujcic.timetracking.core.workspace.entity.WorkspaceType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationDTO {
    private UUID id;
    private WorkspaceType workspaceType;
    private UUID organizationId;
    private NotificationType type;
    private NotificationStatus status;
    private String message;
    private String subjectType;
    private UUID subjectId;
    private String subjectLabel;
    private String sourceRoute;
    private String sourceLabel;
    private String reminderKey;
    private UUID recipientUserId;
    private String recipientUsername;
    private String recipientDisplayName;
    private UUID createdByUserId;
    private String createdByUsername;
    private String createdByDisplayName;
    private UUID resolvedByUserId;
    private String resolvedByUsername;
    private String resolvedByDisplayName;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private OffsetDateTime resolvedAt;
    private OffsetDateTime emailEscalationDueAt;
    private OffsetDateTime emailEscalatedAt;
    private boolean canResolve;
    private boolean canReopen;
    private boolean canDismiss;
}
