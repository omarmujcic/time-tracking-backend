package com.omarmujcic.timetracking.core.notifications.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.omarmujcic.timetracking.core.auth.entity.User;
import com.omarmujcic.timetracking.core.workspace.entity.Organization;
import com.omarmujcic.timetracking.core.workspace.entity.WorkspaceType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
public class Notification {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "workspace_type", nullable = false, length = 20)
    private WorkspaceType workspaceType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_user_id")
    private User workspaceUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_user_id")
    private User recipientUser;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    private NotificationStatus status;

    private String message;

    @Column(name = "subject_type", length = 60)
    private String subjectType;

    private UUID subjectId;

    @Column(name = "subject_label", length = 220)
    private String subjectLabel;

    @Column(name = "source_route", length = 260)
    private String sourceRoute;

    @Column(name = "source_label", length = 160)
    private String sourceLabel;

    @Column(name = "reminder_key", length = 220)
    private String reminderKey;

    private OffsetDateTime emailEscalationDueAt;

    private OffsetDateTime emailEscalatedAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    private OffsetDateTime resolvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by_user_id")
    private User resolvedBy;
}
