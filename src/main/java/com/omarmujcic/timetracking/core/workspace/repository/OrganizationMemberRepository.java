package com.omarmujcic.timetracking.core.workspace.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.omarmujcic.timetracking.core.workspace.entity.OrganizationMember;
import com.omarmujcic.timetracking.core.workspace.entity.OrganizationRole;

public interface OrganizationMemberRepository extends JpaRepository<OrganizationMember, UUID> {

    List<OrganizationMember> findByUserIdOrderByOrganizationNameAsc(UUID userId);

    @Query("""
            select member
            from OrganizationMember member
            join fetch member.user
            where member.organization.id = :organizationId
            order by lower(member.user.displayName), lower(member.user.username)
            """)
    List<OrganizationMember> findMembers(@Param("organizationId") UUID organizationId);

    Optional<OrganizationMember> findByOrganizationIdAndUserId(UUID organizationId, UUID userId);

    boolean existsByOrganizationIdAndUserId(UUID organizationId, UUID userId);

    long countByOrganizationIdAndRole(UUID organizationId, OrganizationRole role);
}
