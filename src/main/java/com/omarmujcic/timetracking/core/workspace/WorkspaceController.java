package com.omarmujcic.timetracking.core.workspace;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.omarmujcic.timetracking.core.auth.entity.User;
import com.omarmujcic.timetracking.core.workspace.dto.SetActiveWorkspaceRequestDTO;
import com.omarmujcic.timetracking.core.workspace.dto.WorkspaceDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @GetMapping
    public List<WorkspaceDTO> list(@AuthenticationPrincipal User user) {
        return workspaceService.workspaces(user);
    }

    @PutMapping("/active")
    public List<WorkspaceDTO> setActive(@AuthenticationPrincipal User user,
            @Valid @RequestBody SetActiveWorkspaceRequestDTO request) {
        return workspaceService.setActiveWorkspace(user, request);
    }
}
