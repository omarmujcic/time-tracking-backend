package com.omarmujcic.timetracking.core.timetracking.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.omarmujcic.timetracking.core.timetracking.entity.TimeEntry;
import com.omarmujcic.timetracking.core.workspace.entity.WorkspaceType;

public interface TimeEntryRepository extends JpaRepository<TimeEntry, UUID> {

    List<TimeEntry> findByUserIdOrderByStartedAtDesc(UUID userId);

    List<TimeEntry> findByOrganizationIdOrderByStartedAtDesc(UUID organizationId);

    @Query("select entry from TimeEntry entry join fetch entry.user order by entry.startedAt desc")
    List<TimeEntry> findAllWithUserOrderByStartedAtDesc();

    Optional<TimeEntry> findByIdAndUserId(UUID id, UUID userId);

    Optional<TimeEntry> findByUserIdAndEndedAtIsNull(UUID userId);

    @Query("""
            select entry from TimeEntry entry
            where entry.user.id = :userId
              and entry.endedAt is null
              and entry.organization is null
              and (entry.workspaceType is null or entry.workspaceType = :workspaceType)
            """)
    Optional<TimeEntry> findActivePersonalTimer(@Param("userId") UUID userId,
            @Param("workspaceType") WorkspaceType workspaceType);

    @Query("""
            select entry from TimeEntry entry
            where entry.user.id = :userId
              and entry.endedAt is null
              and entry.organization.id = :organizationId
            """)
    Optional<TimeEntry> findActiveOrganizationTimer(@Param("userId") UUID userId,
            @Param("organizationId") UUID organizationId);

    boolean existsByProjectId(UUID projectId);

    boolean existsByTaskId(UUID taskId);
}
