package com.omarmujcic.timetracking.core.workspace;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.omarmujcic.timetracking.core.auth.entity.User;
import com.omarmujcic.timetracking.core.auth.repository.UserRepository;
import com.omarmujcic.timetracking.core.workspace.dto.JoinOrganizationRequestDTO;
import com.omarmujcic.timetracking.core.workspace.dto.OrganizationDTO;
import com.omarmujcic.timetracking.core.workspace.dto.OrganizationMemberDTO;
import com.omarmujcic.timetracking.core.workspace.dto.OrganizationRequestDTO;
import com.omarmujcic.timetracking.core.workspace.dto.SetActiveWorkspaceRequestDTO;
import com.omarmujcic.timetracking.core.workspace.dto.WorkspaceDTO;
import com.omarmujcic.timetracking.core.workspace.entity.Organization;
import com.omarmujcic.timetracking.core.workspace.entity.OrganizationMember;
import com.omarmujcic.timetracking.core.workspace.entity.OrganizationRole;
import com.omarmujcic.timetracking.core.workspace.entity.WorkspaceType;
import com.omarmujcic.timetracking.core.workspace.mapper.WorkspaceMapper;
import com.omarmujcic.timetracking.core.workspace.repository.OrganizationMemberRepository;
import com.omarmujcic.timetracking.core.workspace.repository.OrganizationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkspaceService {

    private static final String CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private final SecureRandom secureRandom = new SecureRandom();

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final WorkspaceMapper workspaceMapper;

    @Transactional(readOnly = true)
    public List<WorkspaceDTO> workspaces(User user) {
        User managed = managedUser(user);
        UUID activeOrganizationId = managed.getActiveOrganization() == null ? null : managed.getActiveOrganization().getId();
        List<WorkspaceDTO> organizations = organizationMemberRepository.findByUserIdOrderByOrganizationNameAsc(managed.getId())
            .stream()
            .map(member -> workspaceMapper.organizationWorkspace(member,
                    managed.getActiveWorkspaceType() == WorkspaceType.ORGANIZATION
                    && member.getOrganization().getId().equals(activeOrganizationId)))
            .toList();
        java.util.ArrayList<WorkspaceDTO> result = new java.util.ArrayList<>();
        result.add(workspaceMapper.personalWorkspace(managed.getActiveWorkspaceType() == WorkspaceType.PERSONAL));
        result.addAll(organizations);
        return result;
    }

    @Transactional
    public List<WorkspaceDTO> setActiveWorkspace(User user, SetActiveWorkspaceRequestDTO request) {
        User managed = managedUser(user);
        if (request.getType() == WorkspaceType.PERSONAL) {
            workspaceMapper.updateActiveWorkspace(WorkspaceType.PERSONAL, null, managed);
            return workspaces(managed);
        }
        if (request.getOrganizationId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Organization is required");
        }
        OrganizationMember member = membership(user, request.getOrganizationId());
        workspaceMapper.updateActiveWorkspace(WorkspaceType.ORGANIZATION, member.getOrganization(), managed);
        return workspaces(managed);
    }

    @Transactional
    public OrganizationDTO createOrganization(User user, OrganizationRequestDTO request) {
        OffsetDateTime now = now();
        User managed = managedUser(user);
        Organization organization = workspaceMapper.toOrganization(request, uniqueJoinCode(), managed, now);

        Organization savedOrganization = organizationRepository.save(organization);

        OrganizationMember member = workspaceMapper.toMember(savedOrganization, managed, OrganizationRole.OWNER, now);

        OrganizationMember savedMember = organizationMemberRepository.save(member);
        workspaceMapper.updateActiveWorkspace(WorkspaceType.ORGANIZATION, savedOrganization, managed);
        return workspaceMapper.organizationDTO(savedMember);
    }

    @Transactional
    public OrganizationDTO joinOrganization(User user, JoinOrganizationRequestDTO request) {
        User managed = managedUser(user);
        Organization organization = organizationRepository.findByJoinCodeIgnoreCase(request.getJoinCode().trim())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Organization code not found"));
        if (organizationMemberRepository.existsByOrganizationIdAndUserId(organization.getId(), managed.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You are already in this organization");
        }
        OrganizationMember member = workspaceMapper.toMember(organization, managed, OrganizationRole.MEMBER, now());
        OrganizationMember savedMember = organizationMemberRepository.save(member);
        workspaceMapper.updateActiveWorkspace(WorkspaceType.ORGANIZATION, organization, managed);
        return workspaceMapper.organizationDTO(savedMember);
    }

    @Transactional
    public OrganizationDTO updateOrganization(User user, UUID organizationId, OrganizationRequestDTO request) {
        OrganizationMember member = requireManager(user, organizationId);
        workspaceMapper.updateOrganization(request, now(), member.getOrganization());
        return workspaceMapper.organizationDTO(member);
    }

    @Transactional
    public OrganizationDTO regenerateCode(User user, UUID organizationId) {
        OrganizationMember member = requireManager(user, organizationId);
        workspaceMapper.updateOrganizationCode(uniqueJoinCode(), now(), member.getOrganization());
        return workspaceMapper.organizationDTO(member);
    }

    @Transactional(readOnly = true)
    public List<OrganizationMemberDTO> organizationMembers(User user, UUID organizationId) {
        membership(user, organizationId);
        return organizationMemberRepository.findMembers(organizationId).stream()
            .map(workspaceMapper::organizationMemberDTO)
            .toList();
    }

    @Transactional(readOnly = true)
    public OrganizationMember membership(User user, UUID organizationId) {
        return organizationMemberRepository.findByOrganizationIdAndUserId(organizationId, user.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Organization access denied"));
    }

    @Transactional(readOnly = true)
    public OrganizationMember activeOrganizationMembership(User user) {
        User managed = managedUser(user);
        if (managed.getActiveWorkspaceType() != WorkspaceType.ORGANIZATION || managed.getActiveOrganization() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Active workspace is not an organization");
        }
        return membership(managed, managed.getActiveOrganization().getId());
    }

    public boolean canManage(OrganizationRole role) {
        return role == OrganizationRole.OWNER || role == OrganizationRole.ADMIN;
    }

    private OrganizationMember requireManager(User user, UUID organizationId) {
        OrganizationMember member = membership(user, organizationId);
        if (!canManage(member.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Organization manager access required");
        }
        return member;
    }

    private User managedUser(User user) {
        return userRepository.findById(user.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private String uniqueJoinCode() {
        String code;
        do {
            code = randomCode();
        } while (organizationRepository.existsByJoinCodeIgnoreCase(code));
        return code;
    }

    private String randomCode() {
        StringBuilder builder = new StringBuilder("ORG-");
        for (int index = 0; index < 8; index++) {
            builder.append(CODE_ALPHABET.charAt(secureRandom.nextInt(CODE_ALPHABET.length())));
        }
        return builder.toString();
    }

    private OffsetDateTime now() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }
}
