package com.omarmujcic.timetracking.core.tickettrackz.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.omarmujcic.timetracking.core.auth.entity.User;
import com.omarmujcic.timetracking.core.projects.entity.Project;
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
@Table(name = "ticket_trackz_tickets")
@Getter
@Setter
@NoArgsConstructor
public class Ticket {

    @Id
    private UUID id;

    @Column(name = "ticket_key", nullable = false, unique = true, length = 32)
    private String ticketKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "workspace_type", nullable = false, length = 20)
    private WorkspaceType workspaceType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User workspaceUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_user_id")
    private User assignee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "product_context", columnDefinition = "text")
    private String productContext;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private TicketStatus status;

    @Enumerated(EnumType.STRING)
    @Column(length = 8)
    private TicketPriority priority;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "story_points")
    private Integer storyPoints;

    @Column(name = "estimated_hours", precision = 8, scale = 2)
    private BigDecimal estimatedHours;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
