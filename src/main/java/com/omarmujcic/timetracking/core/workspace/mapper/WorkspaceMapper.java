package com.omarmujcic.timetracking.core.workspace.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.omarmujcic.timetracking.core.auth.entity.User;
import com.omarmujcic.timetracking.core.workspace.dto.OrganizationDTO;
import com.omarmujcic.timetracking.core.workspace.dto.OrganizationMemberDTO;
import com.omarmujcic.timetracking.core.workspace.dto.OrganizationRequestDTO;
import com.omarmujcic.timetracking.core.workspace.dto.WorkspaceDTO;
import com.omarmujcic.timetracking.core.workspace.entity.Organization;
import com.omarmujcic.timetracking.core.workspace.entity.OrganizationMember;
import com.omarmujcic.timetracking.core.workspace.entity.OrganizationRole;
import com.omarmujcic.timetracking.core.workspace.entity.WorkspaceType;

@Mapper(componentModel = "spring", imports = {WorkspaceType.class, OrganizationRole.class, java.util.UUID.class})
public interface WorkspaceMapper {

    @Mapping(target = "type", expression = "java(WorkspaceType.PERSONAL)")
    @Mapping(target = "organizationId", ignore = true)
    @Mapping(target = "name", constant = "Personal")
    @Mapping(target = "legalName", ignore = true)
    @Mapping(target = "joinCode", ignore = true)
    @Mapping(target = "businessAddressLine1", ignore = true)
    @Mapping(target = "businessAddressLine2", ignore = true)
    @Mapping(target = "businessPostalCode", ignore = true)
    @Mapping(target = "businessCity", ignore = true)
    @Mapping(target = "businessCountry", ignore = true)
    @Mapping(target = "timezone", ignore = true)
    @Mapping(target = "defaultCurrency", constant = "EUR")
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "membersCanCreateTasks", constant = "false")
    @Mapping(target = "active", source = "active")
    WorkspaceDTO personalWorkspace(Boolean active);

    @Mapping(target = "type", expression = "java(WorkspaceType.ORGANIZATION)")
    @Mapping(target = "organizationId", source = "member.organization.id")
    @Mapping(target = "name", source = "member.organization.name")
    @Mapping(target = "legalName", source = "member.organization.legalName")
    @Mapping(target = "joinCode", source = "member.organization.joinCode")
    @Mapping(target = "businessAddressLine1", source = "member.organization.businessAddressLine1")
    @Mapping(target = "businessAddressLine2", source = "member.organization.businessAddressLine2")
    @Mapping(target = "businessPostalCode", source = "member.organization.businessPostalCode")
    @Mapping(target = "businessCity", source = "member.organization.businessCity")
    @Mapping(target = "businessCountry", source = "member.organization.businessCountry")
    @Mapping(target = "timezone", source = "member.organization.timezone")
    @Mapping(target = "defaultCurrency", source = "member.organization.defaultCurrency")
    @Mapping(target = "role", source = "member.role")
    @Mapping(target = "membersCanCreateTasks", source = "member.organization.membersCanCreateTasks")
    @Mapping(target = "active", source = "active")
    WorkspaceDTO organizationWorkspace(OrganizationMember member, boolean active);

    @Mapping(target = "id", source = "member.organization.id")
    @Mapping(target = "name", source = "member.organization.name")
    @Mapping(target = "legalName", source = "member.organization.legalName")
    @Mapping(target = "joinCode", source = "member.organization.joinCode")
    @Mapping(target = "businessAddressLine1", source = "member.organization.businessAddressLine1")
    @Mapping(target = "businessAddressLine2", source = "member.organization.businessAddressLine2")
    @Mapping(target = "businessPostalCode", source = "member.organization.businessPostalCode")
    @Mapping(target = "businessCity", source = "member.organization.businessCity")
    @Mapping(target = "businessCountry", source = "member.organization.businessCountry")
    @Mapping(target = "timezone", source = "member.organization.timezone")
    @Mapping(target = "defaultCurrency", source = "member.organization.defaultCurrency")
    @Mapping(target = "role", source = "member.role")
    @Mapping(target = "membersCanCreateTasks", source = "member.organization.membersCanCreateTasks")
    OrganizationDTO organizationDTO(OrganizationMember member);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "displayName", source = "user.displayName")
    OrganizationMemberDTO organizationMemberDTO(OrganizationMember member);

    @Mapping(target = "id", expression = "java(java.util.UUID.randomUUID())")
    @Mapping(target = "name", expression = "java(request.getName().trim())")
    @Mapping(target = "legalName", expression = "java(clean(request.getLegalName()))")
    @Mapping(target = "joinCode", source = "joinCode")
    @Mapping(target = "businessAddressLine1", expression = "java(clean(request.getBusinessAddressLine1()))")
    @Mapping(target = "businessAddressLine2", expression = "java(clean(request.getBusinessAddressLine2()))")
    @Mapping(target = "businessPostalCode", expression = "java(clean(request.getBusinessPostalCode()))")
    @Mapping(target = "businessCity", expression = "java(clean(request.getBusinessCity()))")
    @Mapping(target = "businessCountry", expression = "java(clean(request.getBusinessCountry()))")
    @Mapping(target = "timezone", expression = "java(defaultString(request.getTimezone(), \"UTC\"))")
    @Mapping(target = "defaultCurrency", expression = "java(defaultCurrency(request.getDefaultCurrency(), \"EUR\"))")
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "createdAt", source = "now")
    @Mapping(target = "updatedAt", source = "now")
    @Mapping(target = "billingName", ignore = true)
    @Mapping(target = "billingContactPerson", ignore = true)
    @Mapping(target = "billingAddressLine1", ignore = true)
    @Mapping(target = "billingAddressLine2", ignore = true)
    @Mapping(target = "billingPostalCode", ignore = true)
    @Mapping(target = "billingCity", ignore = true)
    @Mapping(target = "billingCountry", ignore = true)
    @Mapping(target = "billingEmail", ignore = true)
    @Mapping(target = "billingPhone", ignore = true)
    @Mapping(target = "billingTaxId", ignore = true)
    @Mapping(target = "billingRegistrationNumber", ignore = true)
    @Mapping(target = "membersCanCreateTasks", expression = "java(request.getMembersCanCreateTasks() == null || request.getMembersCanCreateTasks())")
    Organization toOrganization(OrganizationRequestDTO request, String joinCode, User createdBy,
            java.time.OffsetDateTime now);

    @Mapping(target = "id", expression = "java(java.util.UUID.randomUUID())")
    @Mapping(target = "organization", source = "organization")
    @Mapping(target = "user", source = "user")
    @Mapping(target = "role", source = "role")
    @Mapping(target = "joinedAt", source = "joinedAt")
    OrganizationMember toMember(Organization organization, User user, OrganizationRole role,
            java.time.OffsetDateTime joinedAt);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "joinCode", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "name", expression = "java(request.getName().trim())")
    @Mapping(target = "legalName", expression = "java(clean(request.getLegalName()))")
    @Mapping(target = "businessAddressLine1", expression = "java(clean(request.getBusinessAddressLine1()))")
    @Mapping(target = "businessAddressLine2", expression = "java(clean(request.getBusinessAddressLine2()))")
    @Mapping(target = "businessPostalCode", expression = "java(clean(request.getBusinessPostalCode()))")
    @Mapping(target = "businessCity", expression = "java(clean(request.getBusinessCity()))")
    @Mapping(target = "businessCountry", expression = "java(clean(request.getBusinessCountry()))")
    @Mapping(target = "timezone", expression = "java(defaultString(request.getTimezone(), organization.getTimezone() == null ? \"UTC\" : organization.getTimezone()))")
    @Mapping(target = "defaultCurrency", expression = "java(defaultCurrency(request.getDefaultCurrency(), organization.getDefaultCurrency() == null ? \"EUR\" : organization.getDefaultCurrency()))")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "billingName", ignore = true)
    @Mapping(target = "billingContactPerson", ignore = true)
    @Mapping(target = "billingAddressLine1", ignore = true)
    @Mapping(target = "billingAddressLine2", ignore = true)
    @Mapping(target = "billingPostalCode", ignore = true)
    @Mapping(target = "billingCity", ignore = true)
    @Mapping(target = "billingCountry", ignore = true)
    @Mapping(target = "billingEmail", ignore = true)
    @Mapping(target = "billingPhone", ignore = true)
    @Mapping(target = "billingTaxId", ignore = true)
    @Mapping(target = "billingRegistrationNumber", ignore = true)
    @Mapping(target = "membersCanCreateTasks", expression = "java(request.getMembersCanCreateTasks() == null ? organization.isMembersCanCreateTasks() : request.getMembersCanCreateTasks())")
    void updateOrganization(OrganizationRequestDTO request, java.time.OffsetDateTime updatedAt,
            @MappingTarget Organization organization);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "joinCode", source = "joinCode")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "legalName", ignore = true)
    @Mapping(target = "businessAddressLine1", ignore = true)
    @Mapping(target = "businessAddressLine2", ignore = true)
    @Mapping(target = "businessPostalCode", ignore = true)
    @Mapping(target = "businessCity", ignore = true)
    @Mapping(target = "businessCountry", ignore = true)
    @Mapping(target = "timezone", ignore = true)
    @Mapping(target = "defaultCurrency", ignore = true)
    @Mapping(target = "billingName", ignore = true)
    @Mapping(target = "billingContactPerson", ignore = true)
    @Mapping(target = "billingAddressLine1", ignore = true)
    @Mapping(target = "billingAddressLine2", ignore = true)
    @Mapping(target = "billingPostalCode", ignore = true)
    @Mapping(target = "billingCity", ignore = true)
    @Mapping(target = "billingCountry", ignore = true)
    @Mapping(target = "billingEmail", ignore = true)
    @Mapping(target = "billingPhone", ignore = true)
    @Mapping(target = "billingTaxId", ignore = true)
    @Mapping(target = "billingRegistrationNumber", ignore = true)
    @Mapping(target = "membersCanCreateTasks", ignore = true)
    void updateOrganizationCode(String joinCode, java.time.OffsetDateTime updatedAt,
            @MappingTarget Organization organization);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "displayName", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "phone", ignore = true)
    @Mapping(target = "activeWorkspaceType", source = "workspaceType")
    @Mapping(target = "activeOrganization", source = "organization")
    void updateActiveWorkspace(WorkspaceType workspaceType, Organization organization, @MappingTarget User user);

    default String clean(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    default String defaultString(String value, String fallback) {
        String cleaned = clean(value);
        return cleaned == null ? fallback : cleaned;
    }

    default String defaultCurrency(String value, String fallback) {
        return defaultString(value, fallback).toUpperCase(java.util.Locale.ROOT);
    }
}
