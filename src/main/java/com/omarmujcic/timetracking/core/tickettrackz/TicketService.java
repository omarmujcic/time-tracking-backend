package com.omarmujcic.timetracking.core.tickettrackz;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.omarmujcic.timetracking.core.auth.entity.User;
import com.omarmujcic.timetracking.core.projects.ProjectService;
import com.omarmujcic.timetracking.core.projects.entity.Project;
import com.omarmujcic.timetracking.core.tickettrackz.dto.CreateTicketRequestDTO;
import com.omarmujcic.timetracking.core.tickettrackz.dto.TicketDTO;
import com.omarmujcic.timetracking.core.tickettrackz.dto.UpdateTicketRequestDTO;
import com.omarmujcic.timetracking.core.tickettrackz.entity.Ticket;
import com.omarmujcic.timetracking.core.tickettrackz.mapper.TicketMapper;
import com.omarmujcic.timetracking.core.tickettrackz.repository.TicketRepository;
import com.omarmujcic.timetracking.core.workspace.WorkspaceService;
import com.omarmujcic.timetracking.core.workspace.entity.Organization;
import com.omarmujcic.timetracking.core.workspace.entity.OrganizationMember;
import com.omarmujcic.timetracking.core.workspace.entity.WorkspaceType;
import com.omarmujcic.timetracking.core.workspace.repository.OrganizationMemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TicketService {

    private static final String FALLBACK_TICKET_KEY_PREFIX = "TKT";

    private final TicketRepository ticketRepository;
    private final TicketMapper ticketMapper;
    private final ProjectService projectService;
    private final WorkspaceService workspaceService;
    private final OrganizationMemberRepository organizationMemberRepository;

    @Transactional(readOnly = true)
    public List<TicketDTO> list(User user, boolean assignedToMe) {
        TicketWorkspace workspace = activeWorkspace(user);
        List<Ticket> tickets;
        if (workspace.workspaceType() == WorkspaceType.ORGANIZATION) {
            UUID organizationId = workspace.organization().getId();
            tickets = assignedToMe
                ? ticketRepository.findByWorkspaceTypeAndOrganizationIdAndAssigneeIdOrderByCreatedAtDesc(
                    WorkspaceType.ORGANIZATION, organizationId, user.getId())
                : ticketRepository.findByWorkspaceTypeAndOrganizationIdOrderByCreatedAtDesc(
                    WorkspaceType.ORGANIZATION, organizationId);
        } else {
            tickets = assignedToMe
                ? ticketRepository.findByWorkspaceTypeAndWorkspaceUserIdAndAssigneeIdOrderByCreatedAtDesc(
                    WorkspaceType.PERSONAL, user.getId(), user.getId())
                : ticketRepository.findByWorkspaceTypeAndWorkspaceUserIdOrderByCreatedAtDesc(
                    WorkspaceType.PERSONAL, user.getId());
        }
        return tickets.stream()
            .map(ticketMapper::toDTO)
            .toList();
    }

    @Transactional(readOnly = true)
    public TicketDTO find(User user, String idOrKey) {
        return ticketMapper.toDTO(findTicketInActiveWorkspace(user, idOrKey));
    }

    @Transactional
    public TicketDTO create(User user, CreateTicketRequestDTO request) {
        TicketWorkspace workspace = activeWorkspace(user);
        assertCanCreateTickets(workspace);
        Project project = projectService.findAccessibleProject(user, request.getProjectId());
        User assignee = assignee(user, workspace, request.getAssigneeUserId());
        OffsetDateTime now = now();
        Ticket ticket = ticketMapper.toEntity(
                request,
                nextTicketKey(project),
                workspace.workspaceType(),
                workspace.workspaceUser(),
                workspace.organization(),
                project,
                assignee,
                user,
                now
        );
        return ticketMapper.toDTO(ticketRepository.save(ticket));
    }

    @Transactional
    public TicketDTO update(User user, String idOrKey, UpdateTicketRequestDTO request) {
        Ticket ticket = findTicketInActiveWorkspace(user, idOrKey);
        TicketWorkspace workspace = activeWorkspace(user);
        Project project = projectService.findAccessibleProject(user, request.getProjectId());
        User assignee = assignee(user, workspace, request.getAssigneeUserId());
        ticketMapper.updateEntity(request, project, assignee, now(), ticket);
        ticket.setTicketKey(rekeyTicket(ticket, project));
        return ticketMapper.toDTO(ticket);
    }

    private TicketWorkspace activeWorkspace(User user) {
        if (user.getActiveWorkspaceType() == WorkspaceType.ORGANIZATION) {
            OrganizationMember member = workspaceService.activeOrganizationMembership(user);
            return new TicketWorkspace(WorkspaceType.ORGANIZATION, null, member.getOrganization(), member);
        }
        return new TicketWorkspace(WorkspaceType.PERSONAL, user, null, null);
    }

    private void assertCanCreateTickets(TicketWorkspace workspace) {
        OrganizationMember member = workspace.member();
        if (member != null && !workspaceService.canCreateTasks(member)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ticket creation is disabled for organization members");
        }
    }

    private User assignee(User user, TicketWorkspace workspace, UUID assigneeUserId) {
        if (assigneeUserId == null) {
            return null;
        }
        if (workspace.workspaceType() == WorkspaceType.PERSONAL) {
            if (!user.getId().equals(assigneeUserId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Personal tickets can only be assigned to you");
            }
            return user;
        }
        return organizationMemberRepository
            .findByOrganizationIdAndUserId(workspace.organization().getId(), assigneeUserId)
            .map(OrganizationMember::getUser)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assignee is not in this workspace"));
    }

    private void assertTicketInActiveWorkspace(User user, Ticket ticket) {
        TicketWorkspace workspace = activeWorkspace(user);
        boolean accessible = workspace.workspaceType() == WorkspaceType.ORGANIZATION
            ? ticket.getWorkspaceType() == WorkspaceType.ORGANIZATION
                && ticket.getOrganization() != null
                && ticket.getOrganization().getId().equals(workspace.organization().getId())
            : ticket.getWorkspaceType() == WorkspaceType.PERSONAL
                && ticket.getWorkspaceUser() != null
                && ticket.getWorkspaceUser().getId().equals(user.getId());
        if (!accessible) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found");
        }
    }

    private Ticket findTicketInActiveWorkspace(User user, String idOrKey) {
        Ticket ticket = parseUuid(idOrKey)
            .flatMap(ticketRepository::findWithProjectById)
            .or(() -> ticketRepository.findByTicketKeyIgnoreCase(idOrKey))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));
        assertTicketInActiveWorkspace(user, ticket);
        return ticket;
    }

    private String nextTicketKey(Project project) {
        String prefix = project.getTicketPrefix();
        if (prefix == null || prefix.isBlank()) {
            prefix = FALLBACK_TICKET_KEY_PREFIX;
        }
        return prefix + "-" + ticketRepository.nextTicketKeyValue();
    }

    private String rekeyTicket(Ticket ticket, Project project) {
        String key = ticket.getTicketKey();
        int separatorIndex = key == null ? -1 : key.lastIndexOf('-');
        if (separatorIndex < 0 || separatorIndex == key.length() - 1) {
            return nextTicketKey(project);
        }
        String prefix = project.getTicketPrefix();
        if (prefix == null || prefix.isBlank()) {
            prefix = FALLBACK_TICKET_KEY_PREFIX;
        }
        String candidate = prefix + "-" + key.substring(separatorIndex + 1);
        if (ticketRepository.existsByTicketKeyIgnoreCaseAndIdNot(candidate, ticket.getId())) {
            return nextTicketKey(project);
        }
        return candidate;
    }

    private java.util.Optional<UUID> parseUuid(String value) {
        try {
            return java.util.Optional.of(UUID.fromString(value));
        } catch (IllegalArgumentException exception) {
            return java.util.Optional.empty();
        }
    }

    private OffsetDateTime now() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }

    private record TicketWorkspace(
            WorkspaceType workspaceType,
            User workspaceUser,
            Organization organization,
            OrganizationMember member
    ) {
    }
}
