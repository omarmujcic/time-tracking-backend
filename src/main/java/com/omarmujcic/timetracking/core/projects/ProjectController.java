package com.omarmujcic.timetracking.core.projects;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.omarmujcic.timetracking.core.auth.entity.User;
import com.omarmujcic.timetracking.core.projects.dto.ProjectDTO;
import com.omarmujcic.timetracking.core.projects.dto.TaskDTO;
import com.omarmujcic.timetracking.core.projects.dto.UpsertProjectRequestDTO;
import com.omarmujcic.timetracking.core.projects.dto.UpsertTaskRequestDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public List<ProjectDTO> list(@AuthenticationPrincipal User user) {
        return projectService.list(user);
    }

    @PostMapping
    public ProjectDTO create(@AuthenticationPrincipal User user, @Valid @RequestBody UpsertProjectRequestDTO request) {
        return projectService.create(user, request);
    }

    @PutMapping("/{id}")
    public ProjectDTO update(@AuthenticationPrincipal User user, @PathVariable UUID id,
            @Valid @RequestBody UpsertProjectRequestDTO request) {
        return projectService.update(user, id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@AuthenticationPrincipal User user, @PathVariable UUID id) {
        projectService.delete(user, id);
    }

    @PostMapping("/{projectId}/tasks")
    public TaskDTO createTask(@AuthenticationPrincipal User user, @PathVariable UUID projectId,
            @Valid @RequestBody UpsertTaskRequestDTO request) {
        return projectService.createTask(user, projectId, request);
    }

    @PutMapping("/{projectId}/tasks/{taskId}")
    public TaskDTO updateTask(@AuthenticationPrincipal User user, @PathVariable UUID projectId, @PathVariable UUID taskId,
            @Valid @RequestBody UpsertTaskRequestDTO request) {
        return projectService.updateTask(user, projectId, taskId, request);
    }

    @DeleteMapping("/{projectId}/tasks/{taskId}")
    public void deleteTask(@AuthenticationPrincipal User user, @PathVariable UUID projectId, @PathVariable UUID taskId) {
        projectService.deleteTask(user, projectId, taskId);
    }
}
