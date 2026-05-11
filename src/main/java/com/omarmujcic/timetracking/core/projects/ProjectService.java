package com.omarmujcic.timetracking.core.projects;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.omarmujcic.timetracking.core.auth.entity.User;
import com.omarmujcic.timetracking.core.projects.dto.ProjectDTO;
import com.omarmujcic.timetracking.core.projects.dto.TaskDTO;
import com.omarmujcic.timetracking.core.projects.dto.UpsertProjectRequestDTO;
import com.omarmujcic.timetracking.core.projects.dto.UpsertTaskRequestDTO;
import com.omarmujcic.timetracking.core.projects.entity.Project;
import com.omarmujcic.timetracking.core.projects.entity.Task;
import com.omarmujcic.timetracking.core.projects.mapper.ProjectMapper;
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

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final TimeEntryRepository timeEntryRepository;
    private final ProjectMapper projectMapper;
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
        assertUniqueProjectName(user, null, name);

        OffsetDateTime now = now();
        OrganizationMember member = user.getActiveWorkspaceType() == WorkspaceType.ORGANIZATION
            ? workspaceService.activeOrganizationMembership(user)
            : null;
        Project project = projectMapper.toEntity(
                request,
                name,
                member == null ? user : null,
                member == null ? null : member.getOrganization(),
                DEFAULT_CURRENCY,
                now
        );
        return toDTO(projectRepository.save(project));
    }

    @Transactional
    public ProjectDTO update(User user, UUID id, UpsertProjectRequestDTO request) {
        assertCanManageProjects(user);
        Project project = findAccessibleProject(user, id);
        String name = request.getName().trim();
        assertUniqueProjectName(user, id, name);
        projectMapper.updateEntity(request, name, now(), project);
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
        assertCanManageProjects(user);
        Project project = findAccessibleProject(user, projectId);
        String name = request.getName().trim();
        if (taskRepository.existsByProjectIdAndNameIgnoreCase(projectId, name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Task name already exists");
        }
        OffsetDateTime now = now();
        Task task = projectMapper.toTaskEntity(request, name, project, now);
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
        List<TaskDTO> tasks = taskRepository.findByProjectIdOrderByNameAsc(project.getId()).stream()
            .map(projectMapper::toTaskDTO)
            .toList();
        return projectMapper.toDTO(project, tasks);
    }

    private void assertCanManageProjects(User user) {
        if (user.getActiveWorkspaceType() == WorkspaceType.PERSONAL) {
            return;
        }
        if (!workspaceService.canManage(workspaceService.activeOrganizationMembership(user).getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Project manager access required");
        }
    }

    private void assertUniqueProjectName(User user, UUID ignoredId, String name) {
        List<Project> projects = projectsForActiveWorkspace(user);
        boolean duplicate = projects.stream()
            .anyMatch(project -> !project.getId().equals(ignoredId) && project.getName().equalsIgnoreCase(name));
        if (duplicate) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Project name already exists");
        }
    }

    private OffsetDateTime now() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }
}
