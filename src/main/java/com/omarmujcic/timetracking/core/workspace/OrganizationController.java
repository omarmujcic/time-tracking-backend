package com.omarmujcic.timetracking.core.workspace;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.omarmujcic.timetracking.core.auth.entity.User;
import com.omarmujcic.timetracking.core.workspace.dto.JoinOrganizationRequestDTO;
import com.omarmujcic.timetracking.core.workspace.dto.OrganizationDTO;
import com.omarmujcic.timetracking.core.workspace.dto.OrganizationMemberDTO;
import com.omarmujcic.timetracking.core.workspace.dto.OrganizationRequestDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final WorkspaceService workspaceService;

    @PostMapping
    public OrganizationDTO create(@AuthenticationPrincipal User user, @Valid @RequestBody OrganizationRequestDTO request) {
        return workspaceService.createOrganization(user, request);
    }

    @PostMapping("/join")
    public OrganizationDTO join(@AuthenticationPrincipal User user, @Valid @RequestBody JoinOrganizationRequestDTO request) {
        return workspaceService.joinOrganization(user, request);
    }

    @PutMapping("/{id}")
    public OrganizationDTO update(@AuthenticationPrincipal User user, @PathVariable UUID id,
            @Valid @RequestBody OrganizationRequestDTO request) {
        return workspaceService.updateOrganization(user, id, request);
    }

    @PostMapping("/{id}/regenerate-code")
    public OrganizationDTO regenerateCode(@AuthenticationPrincipal User user, @PathVariable UUID id) {
        return workspaceService.regenerateCode(user, id);
    }

    @GetMapping("/{id}/members")
    public List<OrganizationMemberDTO> members(@AuthenticationPrincipal User user, @PathVariable UUID id) {
        return workspaceService.organizationMembers(user, id);
    }
}
