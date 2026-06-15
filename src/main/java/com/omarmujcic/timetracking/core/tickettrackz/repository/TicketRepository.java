package com.omarmujcic.timetracking.core.tickettrackz.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.omarmujcic.timetracking.core.tickettrackz.entity.Ticket;
import com.omarmujcic.timetracking.core.workspace.entity.WorkspaceType;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {

    @Query(value = "select nextval('ticket_trackz_ticket_key_seq')", nativeQuery = true)
    long nextTicketKeyValue();

    @EntityGraph(attributePaths = {"project", "assignee", "createdBy"})
    List<Ticket> findByWorkspaceTypeAndWorkspaceUserIdOrderByCreatedAtDesc(WorkspaceType workspaceType, UUID userId);

    @EntityGraph(attributePaths = {"project", "assignee", "createdBy"})
    List<Ticket> findByWorkspaceTypeAndWorkspaceUserIdAndAssigneeIdOrderByCreatedAtDesc(
            WorkspaceType workspaceType,
            UUID userId,
            UUID assigneeId
    );

    @EntityGraph(attributePaths = {"project", "assignee", "createdBy"})
    List<Ticket> findByWorkspaceTypeAndOrganizationIdOrderByCreatedAtDesc(WorkspaceType workspaceType,
            UUID organizationId);

    @EntityGraph(attributePaths = {"project", "assignee", "createdBy"})
    List<Ticket> findByWorkspaceTypeAndOrganizationIdAndAssigneeIdOrderByCreatedAtDesc(
            WorkspaceType workspaceType,
            UUID organizationId,
            UUID assigneeId
    );

    @EntityGraph(attributePaths = {"project", "assignee", "createdBy"})
    Optional<Ticket> findByTicketKeyIgnoreCase(String ticketKey);

    boolean existsByTicketKeyIgnoreCaseAndIdNot(String ticketKey, UUID id);

    @EntityGraph(attributePaths = {"project", "assignee", "createdBy"})
    @Query("select ticket from Ticket ticket where ticket.id = :id")
    Optional<Ticket> findWithProjectById(@Param("id") UUID id);
}
