package com.omarmujcic.timetracking.core.projects;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.omarmujcic.timetracking.core.auth.entity.User;
import com.omarmujcic.timetracking.core.projects.dto.ProjectBillingRuleDTO;
import com.omarmujcic.timetracking.core.projects.dto.ProjectDTO;
import com.omarmujcic.timetracking.core.projects.dto.TaskDTO;
import com.omarmujcic.timetracking.core.projects.dto.UpsertProjectBillingRuleRequestDTO;
import com.omarmujcic.timetracking.core.projects.dto.UpsertProjectRequestDTO;
import com.omarmujcic.timetracking.core.projects.dto.UpsertTaskRequestDTO;
import com.omarmujcic.timetracking.core.projects.entity.Project;
import com.omarmujcic.timetracking.core.projects.entity.ProjectBillingRule;
import com.omarmujcic.timetracking.core.projects.entity.ProjectBillingRuleType;
import com.omarmujcic.timetracking.core.projects.entity.Task;
import com.omarmujcic.timetracking.core.projects.entity.TaskStatus;
import com.omarmujcic.timetracking.core.projects.mapper.ProjectBillingRuleMapper;
import com.omarmujcic.timetracking.core.projects.mapper.ProjectMapper;
import com.omarmujcic.timetracking.core.projects.repository.ProjectBillingRuleRepository;
import com.omarmujcic.timetracking.core.projects.repository.ProjectRepository;
import com.omarmujcic.timetracking.core.projects.repository.TaskRepository;
import com.omarmujcic.timetracking.core.timetracking.repository.TimeEntryRepository;
import com.omarmujcic.timetracking.core.workspace.WorkspaceService;
import com.omarmujcic.timetracking.core.workspace.entity.OrganizationMember;
import com.omarmujcic.timetracking.core.workspace.entity.WorkspaceType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private static final String DEFAULT_CURRENCY = "EUR";
    private static final int DEFAULT_TICKET_PREFIX_LENGTH = 3;
    private static final int MIN_TICKET_PREFIX_LENGTH = 2;
    private static final int MAX_TICKET_PREFIX_LENGTH = 12;
    private static final String FALLBACK_TICKET_PREFIX = "PR";

    private final ProjectRepository projectRepository;
    private final ProjectBillingRuleRepository billingRuleRepository;
    private final TaskRepository taskRepository;
    private final TimeEntryRepository timeEntryRepository;
    private final ProjectMapper projectMapper;
    private final ProjectBillingRuleMapper billingRuleMapper;
    private final WorkspaceService workspaceService;

    @Transactional(readOnly = true)
    public List<ProjectDTO> list(User user) {
        return projectsForActiveWorkspace(user).stream()
            .map(this::toDTO)
            .toList();
    }

    @Transactional
    public ProjectDTO create(User user, UpsertProjectRequestDTO request) {
        assertCanManageProjects(user);
        String name = request.getName().trim();
        List<Project> projects = projectsForActiveWorkspace(user);
        assertUniqueProjectName(projects, null, name);
        String ticketPrefix = ticketPrefixForRequest(request, projects, null, name);
        assertUniqueTicketPrefix(projects, null, ticketPrefix);
        assertValidBillingRule(request.getBillingRule());

        OffsetDateTime now = now();
        OrganizationMember member = user.getActiveWorkspaceType() == WorkspaceType.ORGANIZATION
            ? workspaceService.activeOrganizationMembership(user)
            : null;
        Project project = projectMapper.toEntity(
                request,
                name,
                ticketPrefix,
                member == null ? user : null,
                member == null ? null : member.getOrganization(),
                DEFAULT_CURRENCY,
                now
        );
        Project saved = projectRepository.save(project);
        saveBillingRule(saved, request.getBillingRule(), now);
        return toDTO(saved);
    }

    @Transactional
    public ProjectDTO update(User user, UUID id, UpsertProjectRequestDTO request) {
        assertCanManageProjects(user);
        Project project = findAccessibleProject(user, id);
        String name = request.getName().trim();
        List<Project> projects = projectsForActiveWorkspace(user);
        assertUniqueProjectName(projects, id, name);
        String ticketPrefix = ticketPrefixForRequest(request, projects, id, name);
        assertUniqueTicketPrefix(projects, id, ticketPrefix);
        assertValidBillingRule(request.getBillingRule());
        OffsetDateTime now = now();
        projectMapper.updateEntity(request, name, ticketPrefix, now, project);
        saveBillingRule(project, request.getBillingRule(), now);
        return toDTO(project);
    }

    @Transactional
    public void delete(User user, UUID id) {
        assertCanManageProjects(user);
        Project project = findAccessibleProject(user, id);
        if (timeEntryRepository.existsByProjectId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Project is used by time entries");
        }
        projectRepository.delete(project);
    }

    @Transactional
    public TaskDTO createTask(User user, UUID projectId, UpsertTaskRequestDTO request) {
        TaskStatus status = taskStatusForNewTask(user, request);
        Project project = findAccessibleProject(user, projectId);
        String name = request.getName().trim();
        if (taskRepository.existsByProjectIdAndNameIgnoreCase(projectId, name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Task name already exists");
        }
        OffsetDateTime now = now();
        Task task = projectMapper.toTaskEntity(request, name, status, project, now);
        return projectMapper.toTaskDTO(taskRepository.save(task));
    }

    @Transactional
    public TaskDTO updateTask(User user, UUID projectId, UUID taskId, UpsertTaskRequestDTO request) {
        assertCanManageProjects(user);
        findAccessibleProject(user, projectId);
        Task task = taskRepository.findByIdAndProjectId(taskId, projectId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        String name = request.getName().trim();
        if (!task.getName().equalsIgnoreCase(name) && taskRepository.existsByProjectIdAndNameIgnoreCase(projectId, name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Task name already exists");
        }
        projectMapper.updateTaskEntity(request, name, now(), task);
        return projectMapper.toTaskDTO(task);
    }

    @Transactional
    public void deleteTask(User user, UUID projectId, UUID taskId) {
        assertCanManageProjects(user);
        findAccessibleProject(user, projectId);
        Task task = taskRepository.findByIdAndProjectId(taskId, projectId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        if (timeEntryRepository.existsByTaskId(taskId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Task is used by time entries");
        }
        taskRepository.delete(task);
    }

    @Transactional(readOnly = true)
    public Project findAccessibleProject(User user, UUID id) {
        if (user.getActiveWorkspaceType() == WorkspaceType.ORGANIZATION) {
            OrganizationMember member = workspaceService.activeOrganizationMembership(user);
            return projectRepository.findByIdAndOrganizationId(id, member.getOrganization().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        }
        return projectRepository.findByIdAndUserId(id, user.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
    }

    @Transactional(readOnly = true)
    public Task findAccessibleTask(User user, UUID projectId, UUID taskId) {
        findAccessibleProject(user, projectId);
        return taskRepository.findByIdAndProjectId(taskId, projectId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
    }

    private List<Project> projectsForActiveWorkspace(User user) {
        if (user.getActiveWorkspaceType() == WorkspaceType.ORGANIZATION) {
            OrganizationMember member = workspaceService.activeOrganizationMembership(user);
            return projectRepository.findByOrganizationIdOrderByNameAsc(member.getOrganization().getId());
        }
        return projectRepository.findByUserIdOrderByNameAsc(user.getId());
    }

    private ProjectDTO toDTO(Project project) {
        ProjectBillingRuleDTO billingRule = billingRuleRepository.findFirstByProjectIdOrderByEffectiveFromDesc(project.getId())
            .map(billingRuleMapper::toDTO)
            .orElse(null);
        List<TaskDTO> tasks = taskRepository.findByProjectIdOrderByNameAsc(project.getId()).stream()
            .map(projectMapper::toTaskDTO)
            .toList();
        return projectMapper.toDTO(project, billingRule, tasks);
    }

    private void saveBillingRule(Project project, UpsertProjectBillingRuleRequestDTO request, OffsetDateTime now) {
        LocalDate effectiveFrom = request.getEffectiveFrom().withDayOfMonth(1);
        ProjectBillingRule rule = billingRuleRepository.findByProjectIdAndEffectiveFrom(project.getId(), effectiveFrom)
            .orElse(null);
        if (rule == null) {
            billingRuleRepository.save(billingRuleMapper.toEntity(request, project, now));
            return;
        }
        billingRuleMapper.updateEntity(request, now, rule);
    }

    private void assertValidBillingRule(UpsertProjectBillingRuleRequestDTO rule) {
        if (rule == null || rule.getType() == null || rule.getEffectiveFrom() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Billing rule is required");
        }
        if (rule.getType() == ProjectBillingRuleType.HOURLY) {
            return;
        }
        if (rule.getType() == ProjectBillingRuleType.FIXED_MONTHLY) {
            assertNonNegative(rule.getMonthlyAmount(), "Monthly amount is required");
            return;
        }
        assertNonNegative(rule.getBaseAmount(), "Base amount is required");
        assertNonNegative(rule.getIncludedHours(), "Included hours are required");
        assertNonNegative(rule.getOverageHourlyRate(), "Overage hourly rate is required");
    }

    private void assertNonNegative(BigDecimal value, String message) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }

    private void assertCanManageProjects(User user) {
        if (user.getActiveWorkspaceType() == WorkspaceType.PERSONAL) {
            return;
        }
        if (!workspaceService.canManage(workspaceService.activeOrganizationMembership(user).getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Project manager access required");
        }
    }

    private TaskStatus taskStatusForNewTask(User user, UpsertTaskRequestDTO request) {
        if (user.getActiveWorkspaceType() == WorkspaceType.PERSONAL) {
            return request.getStatus();
        }
        OrganizationMember member = workspaceService.activeOrganizationMembership(user);
        if (!workspaceService.canCreateTasks(member)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Task creation is disabled for organization members");
        }
        return workspaceService.canManage(member.getRole()) ? request.getStatus() : TaskStatus.ACTIVE;
    }

    private void assertUniqueProjectName(List<Project> projects, UUID ignoredId, String name) {
        boolean duplicate = projects.stream()
            .anyMatch(project -> !project.getId().equals(ignoredId) && project.getName().equalsIgnoreCase(name));
        if (duplicate) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Project name already exists");
        }
    }

    private String ticketPrefixForRequest(UpsertProjectRequestDTO request, List<Project> projects, UUID ignoredId,
            String name) {
        String requestedPrefix = normalizeTicketPrefix(request.getTicketPrefix());
        if (requestedPrefix != null) {
            return requestedPrefix;
        }
        return uniqueGeneratedTicketPrefix(projects, ignoredId, name);
    }

    private String normalizeTicketPrefix(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
        if (normalized.length() < MIN_TICKET_PREFIX_LENGTH || normalized.length() > MAX_TICKET_PREFIX_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Project ticket prefix must be 2 to 12 letters or numbers");
        }
        return normalized;
    }

    private String uniqueGeneratedTicketPrefix(List<Project> projects, UUID ignoredId, String name) {
        String base = baseTicketPrefix(name);
        String candidate = base;
        int suffix = 2;
        while (ticketPrefixExists(projects, ignoredId, candidate)) {
            String suffixValue = String.valueOf(suffix);
            int baseLength = Math.min(base.length(), MAX_TICKET_PREFIX_LENGTH - suffixValue.length());
            candidate = base.substring(0, Math.max(MIN_TICKET_PREFIX_LENGTH, baseLength)) + suffixValue;
            suffix++;
        }
        return candidate;
    }

    private String baseTicketPrefix(String name) {
        String normalized = name.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
        if (normalized.length() >= DEFAULT_TICKET_PREFIX_LENGTH) {
            return normalized.substring(0, DEFAULT_TICKET_PREFIX_LENGTH);
        }
        if (normalized.length() == MIN_TICKET_PREFIX_LENGTH) {
            return normalized;
        }
        if (normalized.length() == 1) {
            return normalized + "X";
        }
        return FALLBACK_TICKET_PREFIX;
    }

    private void assertUniqueTicketPrefix(List<Project> projects, UUID ignoredId, String ticketPrefix) {
        if (ticketPrefixExists(projects, ignoredId, ticketPrefix)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Project ticket prefix already exists");
        }
    }

    private boolean ticketPrefixExists(List<Project> projects, UUID ignoredId, String ticketPrefix) {
        return projects.stream()
            .anyMatch(project -> !project.getId().equals(ignoredId)
                    && project.getTicketPrefix() != null
                    && project.getTicketPrefix().equalsIgnoreCase(ticketPrefix));
    }

    private OffsetDateTime now() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }
}
